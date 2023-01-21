package searchengine.model;

import javax.persistence.*;

@Entity
public class Indexing {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    private Page page;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    private Lemma lemma;

    @Column(columnDefinition = "FLOAT", nullable = false)
    private float ranking;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Lemma getLemma() {
        return lemma;
    }

    public void setLemma(Lemma lemma) {
        this.lemma = lemma;
    }

    public float getRanking() {
        return ranking;
    }

    public void setRanking(float ranking) {
        this.ranking = ranking;
    }
}