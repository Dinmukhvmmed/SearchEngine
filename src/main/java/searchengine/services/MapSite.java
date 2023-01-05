package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.RecursiveTask;

import static java.lang.Thread.sleep;

public class MapSite extends RecursiveTask<Set<Page>> implements Comparable<MapSite> {
    private Site site = new Site();
    private Page page = new Page();
    private static Set<Page> allPages = new TreeSet<>();

    public MapSite(Page page) {
        this.page = page;
    }

    public MapSite(Site site) {
        this.site = site;
    }

    @Override
    protected Set<Page> compute() {
        try {
            sleep(150);
            homeSiteToPage(site);
            Connection connection = Jsoup.connect(page.getPath()).timeout(50000);
            Document document = connection
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            Elements elements = document.select("body").select("a");
            List<Page> childrenPages = new ArrayList<>();

            for (Element element : elements) {
                String childUrl = element.absUrl("href");
                Page newPage = new Page();
                newPage.setPath(childUrl);
                newPage.setContent(document.toString());
                newPage.setCode(200);
                if (isCorrectAndUniqueUrl(newPage)) {
                    childrenPages.add(newPage);
                    allPages.add(newPage);
                }
            }

            List<MapSite> taskList = new ArrayList<>();

            for (Page childPage : childrenPages) {
                MapSite task = new MapSite(childPage);
                task.fork();
                taskList.add(task);
            }

            for (MapSite task : taskList) {
                allPages.addAll(task.join());
            }

        } catch (Exception e) {
            System.out.println("Не удалось проиндексировать страницу: " + page.getPath());
        }
        return allPages;
    }

    private void homeSiteToPage(Site site) {
        if (page.getPath() == null) {
            page.setPath(site.getUrl());
            page.setCode(200);
            page.setContent("-");
            allPages.add(page);
        }
    }

    public boolean isCorrectAndUniqueUrl(Page examplePage) {
        boolean isTrueDomain = examplePage.getPath().contains(page.getPath());
        boolean isUnique = !allPages.contains(examplePage);
        boolean isNotInnerElement = !examplePage.getPath().contains("#");
        boolean isNotPdf = !examplePage.getPath().endsWith(".pdf");
        boolean isNotJpg = !examplePage.getPath().endsWith(".jpg");
        if (isTrueDomain && isUnique && isNotInnerElement && isNotPdf && isNotJpg) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(MapSite o) {
        return this.page.getPath().compareTo(o.page.getPath());
    }
}
