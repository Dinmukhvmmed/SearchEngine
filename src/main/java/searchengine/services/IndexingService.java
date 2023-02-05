package searchengine.services;

import com.google.common.collect.Iterables;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.lemmatization.LemmasWorker;
import searchengine.services.thread.MapSite;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
public class IndexingService {
    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    LemmaRepository lemmaRepository;

    @Autowired
    IndexRepository indexingRepository;

    @Autowired
    SitesList sitesList;

    Set<Site> sites = new TreeSet<>();
    LemmasWorker lemmasWorker = new LemmasWorker();


    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        Iterable<Site> siteIterable = siteRepository.findAll();
        total.setSites(Iterables.size(siteIterable));
        total.setIndexing(true);

        for (Site site : siteIterable) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = pagesCount(site);
            int lemmas = lemmasCount(site);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(String.valueOf(site.getStatus()));
            item.setError(site.getLastError());
            ZonedDateTime zonedDateTime = ZonedDateTime.of(site.getStatusTime(), ZoneId.systemDefault());
            item.setStatusTime(zonedDateTime.toInstant().toEpochMilli());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    public int pagesCount(Site site) {
        int count = 0;
        Iterable<Page> pageIterable = pageRepository.findAll();
        for (Page pageFromDB : pageIterable) {
            if (pageFromDB.getSite().equals(site)) {
                count++;
            }
        }
        return count;
    }

    private int lemmasCount(Site site) {
        int count = 0;
        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
        for (Lemma lemmaFromDB : lemmaIterable) {
            if (lemmaFromDB.getSite().equals(site)) {
                count++;
            }
        }
        return count;
    }

    public void delete() {
        siteRepository.deleteAll();
        pageRepository.deleteAll();
    }


    public Boolean checkIndexing() {
        for (Site site : sites) {
            if (site.getStatus().equals(Status.INDEXING)) {
                return true;
            }
        }
        return false;
    }

    public void startService() {
        for (searchengine.config.Site siteFromConfig : sitesList.getSites()) {
            Site site = new Site();
            site.setName(siteFromConfig.getName());
            site.setUrl(siteFromConfig.getUrl());
            site.setStatus(Status.INDEXING);
            siteRepository.save(site);
            sites.add(site);
            new Thread(() -> crawlingPages(site)).start();
        }
    }

    private synchronized void crawlingPages(Site site) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        MapSite mapSite = new MapSite(site);
        forkJoinPool.execute(mapSite);
        Set<Page> pages = mapSite.join();

        for (Page page : pages) {
            pageRepository.save(page);
            startLemmasWorker(page);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }

        site.setStatus(Status.INDEXED);
        siteRepository.save(site);
    }

    private void startLemmasWorker(Page page) {
        try {
            HashMap<String, Integer> lemmas = lemmasWorker.iterator(page.getContent());
            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                Lemma lemma = createAndSaveLemma(page, entry.getKey());
                saveIndex(page, lemma, entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Lemma createAndSaveLemma(Page page, String word) {
        Lemma newLemma = new Lemma();
        newLemma.setSite(page.getSite());
        newLemma.setLemma(word);
        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();

        for (Lemma lemmaFromDB : lemmaIterable) {
            if (lemmaFromDB.equals(newLemma)) {
                if (lemmaFromDB.getFrequency() < pagesCount(lemmaFromDB.getSite())
                        && lemmaFromDB.getSite().getStatus().equals(Status.INDEXING)) {
                    lemmaFromDB.setFrequency(lemmaFromDB.getFrequency() + 1);
                    lemmaRepository.save(lemmaFromDB);
                }
                return lemmaFromDB;
            }
        }

        newLemma.setFrequency(1);
        lemmaRepository.save(newLemma);
        return newLemma;
    }

    private void saveIndex(Page page, Lemma lemma, Integer rank) {
        Indexing index = new Indexing();
        index.setPage(page);
        index.setLemma(lemma);
        index.setRanking(rank);
        if (index.getPage().getSite().getStatus().equals(Status.INDEXING)) {
            indexingRepository.save(index);
        }
    }

    public void stopService() {
        for (Site site : sites) {
            if (site.getStatus().equals(Status.INDEXING)) {
                Thread.currentThread().interrupt();
                site.setStatus(Status.FAILED);
                site.setStatusTime(LocalDateTime.now());
                site.setLastError("Индексация остановлена пользователем");
                siteRepository.save(site);
            }
        }
    }

    public Boolean checkURL(String url) {
        String regex1 = "https?://[a-z0-9]{3,30}.[a-z]{2,3}/";
        String regex2 = "https?://www.[a-z0-9]{3,30}.[a-z]{2,3}/";
        return url.matches(regex1) || url.matches(regex2);
    }

    public void indexingOnePage(String url) {
        Site site = checkSiteInDB(url);
        Page page = new Page();
        page.setSite(site);
        page.setPath(url);
        cleanEntities(site, page);

        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
        sites.add(site);

        page.setCode(200);
        page.setContent(getHtmlCode(page).toString());
        pageRepository.save(page);

        startLemmasWorker(page);

        site.setStatusTime(LocalDateTime.now());
        site.setStatus(Status.INDEXED);
        siteRepository.save(site);
    }

    private Site checkSiteInDB(String url) {
        Site newSite = new Site();
        newSite.setUrl(url);
        newSite.setName(url);
        newSite.setStatus(Status.INDEXING);
        Iterable<Site> siteIterator = siteRepository.findAll();
        for (Site siteFromDB : siteIterator) {
            if (siteFromDB.equals(newSite)) {
                return siteFromDB;
            }
        }
        return newSite;
    }

    private void cleanEntities(Site site, Page page) {
        if (site.getStatus().equals(Status.INDEXED)) {
            site.setStatus(Status.INDEXING);

            Iterable<Indexing> indexIterable = indexingRepository.findAll();
            for (Indexing indexFromDB : indexIterable) {
                if (indexFromDB.getPage().getSite().equals(site)) {
                    indexingRepository.delete(indexFromDB);
                }
            }

            Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
            for (Lemma lemmaFromDB : lemmaIterable) {
                if (lemmaFromDB.getSite().equals(site)) {
                    lemmaRepository.delete(lemmaFromDB);
                }
            }

            Iterable<Page> pageIterable = pageRepository.findAll();
            for (Page pageFromDB : pageIterable) {
                if (pageFromDB.equals(page)) {
                    pageRepository.delete(pageFromDB);
                }
            }
        }
    }

    public Document getHtmlCode(Page page) {
        Connection connection = Jsoup.connect(page.getPath()).timeout(50000);
        Document document = null;
        try {
            document = connection
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }
}
