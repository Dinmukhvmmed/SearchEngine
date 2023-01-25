package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.Set;

public class Result {
    private Page page;
    private Set<Lemma> lemmaSet;
    private int absoluteRelevance;
    private float relevance;
    private String path;
    private String title;
    private String snippet;

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public int getAbsoluteRelevance() {
        return absoluteRelevance;
    }

    public void setAbsoluteRelevance(int absoluteRelevance) {
        this.absoluteRelevance = absoluteRelevance;
    }

    public float getRelevance() {
        return relevance;
    }

    public void setRelevance(float relevance) {
        this.relevance = relevance;
    }

    public Set<Lemma> getLemmaSet() {
        return lemmaSet;
    }

    public void setLemmaSet(Set<Lemma> lemmaSet) {
        this.lemmaSet = lemmaSet;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    @Override
    public String toString() {
        return "{" +
                "site : \"" + page.getSite().getUrl() + "\",\n" +
                "siteName : \"" + page.getSite().getName() + "\",\n" +
                "uri : \"" + path + "\",\n" +
                "title : \"" + title + "\",\n" +
                "snippet : \"" + snippet + "\",\n" +
                "relevance : " + relevance + "\",\n" +
                "}";
    }
}
