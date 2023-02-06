package searchengine.services;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.result.DtoResult;
import searchengine.dto.result.ResultMapper;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.lemmatization.LemmasWorker;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {
    @Autowired
    SiteRepository siteRepository;

    @Autowired
    LemmaRepository lemmaRepository;

    @Autowired
    IndexRepository indexingRepository;

    @Autowired
    IndexingService indexingService;

    LemmasWorker lemmasWorker = new LemmasWorker();

    public List<DtoResult> searchingOneSite(String text, Site site) {
        List<Result> resultList = new ArrayList<>();
        try {
            Set<String> words = lemmasWorker.iterator(text).keySet();
            Set<Lemma> lemmaSet = new TreeSet<>();

            for (String searchLemma : words) {
                Lemma lemma = new Lemma();
                lemma.setLemma(searchLemma);
                lemma.setSite(site);
                excludeOftenLemmas(lemma, lemmaSet);
            }

            Set<Page> pageSet = searchPagesForLemma(lemmaSet);
            HashMap<Page, Integer> absoluteRelevance = sumAbsoluteRelevance(pageSet, lemmaSet);
            float maxAbsoluteRelevance = relevanceCoefficient(absoluteRelevance);

            for (Map.Entry<Page, Integer> entry : absoluteRelevance.entrySet()) {
                Result result = new Result();
                Page page = entry.getKey();
                result.setPage(page);
                result.setAbsoluteRelevance(entry.getValue());
                result.setRelevance(entry.getValue() / maxAbsoluteRelevance);
                result.setLemmaSet(lemmaSet);
                result.setPath(page.getPath());
                result.setTitle(indexingService.getHtmlCode(page).title());
                result.setSnippet(searchSnippet(indexingService.getHtmlCode(page), text));
                resultList.add(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transfer(resultList);
    }

    private void excludeOftenLemmas(Lemma lemma, Set<Lemma> lemmaSet) {
        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
        for (Lemma lemmaFromDB : lemmaIterable) {
            if (lemmaFromDB.equals(lemma) && lemmaFromDB.getFrequency() < indexingService.pagesCount(lemma.getSite()) / 3) {
                lemmaSet.add(lemma);
            }
        }
        lemmaSet.stream().sorted(Comparator.comparing(Lemma::getFrequency));
    }

    private Set<Page> searchPagesForLemma(Set<Lemma> lemmaSet) {
        Set<Page> pagesSet = new TreeSet<>();
        Iterable<Indexing> indexingIterable = indexingRepository.findAll();
        for (Lemma lemma : lemmaSet) {
            for (Indexing indexing : indexingIterable) {
                if (indexing.getLemma().equals(lemma)) {
                    pagesSet.add(indexing.getPage());
                }
            }
        }
        return pagesSet;
    }

    private HashMap<Page, Integer> sumAbsoluteRelevance(Set<Page> pageSet, Set<Lemma> lemmaSet) {
        HashMap<Page, Integer> relevanceMap = new HashMap<>();
        Iterable<Indexing> indexingIterable = indexingRepository.findAll();

        for (Page page : pageSet) {
            int absoluteRelevance = 0;
            for (Lemma lemma : lemmaSet) {
                for (Indexing indexing : indexingIterable) {
                    if (indexing.getPage().equals(page) && indexing.getLemma().equals(lemma)) {
                        absoluteRelevance += indexing.getRanking();
                    }
                }
            }
            relevanceMap.put(page, absoluteRelevance);
        }
        return relevanceMap;
    }

    private int relevanceCoefficient(HashMap<Page, Integer> sumAbsoluteRelevance) {
        int i = 0;

        for (Map.Entry<Page, Integer> entry : sumAbsoluteRelevance.entrySet()) {
            if (entry.getValue() > i) {
                i = entry.getValue();
            }
        }

        return i;
    }

    private String searchSnippet(Document html, String text) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] words = text.toLowerCase(Locale.ROOT).replaceAll("([^а-я\\s])", " ")
                .trim().split("\\s+");
        for (String word : words) {
            String content = html.toString().toLowerCase(Locale.ROOT).replaceAll("([^а-я\\s])", " ");
            int start = content.indexOf(word);
            if (start != -1) {
                stringBuilder.append(content.substring(start, start + 40)).append("... \n");
            }
        }
        return stringBuilder.toString();
    }

    private List<DtoResult> transfer(List<Result> resultList) {
        List<DtoResult> dtoResultsList = new ArrayList<>();
        for (Result result : resultList) {
            dtoResultsList.add(ResultMapper.dtoResult(result));
        }
        return dtoResultsList.stream().sorted(Comparator.comparing(DtoResult::getRelevance).reversed()).toList();
    }

    public List<DtoResult> searchingAllSites(String text) {
        List<DtoResult> totalResult = new ArrayList<>();
        Iterable<Site> siteIterable = siteRepository.findAll();
        for (Site site : siteIterable) {
            if (site.getStatus().equals(Status.INDEXED)) {
                totalResult.addAll(searchingOneSite(text, site));
            }
        }
        return totalResult.stream().sorted(Comparator.comparing(DtoResult::getRelevance).reversed()).toList();
    }
}
