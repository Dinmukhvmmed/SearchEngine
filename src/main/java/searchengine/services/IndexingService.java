package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Service
public class IndexingService {
    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    SitesList sitesList;

    List<Site> sites = new ArrayList<>();

    public void delete() {
        siteRepository.deleteAll();
        pageRepository.deleteAll();
    }

    public void startService() {
        for (searchengine.config.Site siteFromConfig : sitesList.getSites()) {
            Site site = new Site();
            site.setName(siteFromConfig.getName());
            site.setUrl(siteFromConfig.getUrl());
            site.setStatus(Status.INDEXING);
            sites.add(site);
            siteRepository.save(site);
            new Thread(() -> {
                ForkJoinPool forkJoinPool = new ForkJoinPool();
                MapSite mapSite = new MapSite(site);
                forkJoinPool.execute(mapSite);
                Set<Page> pages = (mapSite.join());
                System.out.println(pages.size());//1086
                for (Page page : pages) {
                    pageRepository.save(page);
                    site.setStatusTime(LocalDateTime.now());
                    siteRepository.save(site);
                }
                site.setStatus(Status.INDEXED);
                siteRepository.save(site);
            }).start();
        }
    }

    public void stopService() {
        Thread.interrupted();
    }

    public Boolean checkIndexing() {
        for (Site site : sites) {
            if (site.getStatus().equals(Status.INDEXING) || site.getStatus().equals(Status.INDEXED)) {
                return true;
            }
        }
        return false;
    }
}
