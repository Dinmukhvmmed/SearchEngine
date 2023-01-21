package searchengine.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Page implements Comparable<Page> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    private Site site;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return Objects.equals(site, page.site) && Objects.equals(path, page.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, path);
    }

    @Override
    public int compareTo(Page o) {
        return this.getPath().compareTo(o.getPath());
    }
}
