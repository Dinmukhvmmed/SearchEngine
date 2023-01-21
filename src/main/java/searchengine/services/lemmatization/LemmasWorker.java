package searchengine.services.lemmatization;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class LemmasWorker {

    public HashMap<String, Integer> iterator(String html) throws IOException {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        HashMap<String, Integer> hashMap = new HashMap<>();
        for (String word : cleanHtml(html)) {
            String normalForm = morphology.getNormalForms(word).get(0);
            if (!checkWord(morphology, normalForm)) {
                continue;
            }
            if (!hashMap.containsKey(normalForm)) {
                hashMap.put(normalForm, 1);
            } else {
                hashMap.put(normalForm, hashMap.get(normalForm) + 1);
            }
        }
        return hashMap;
    }

    private boolean checkWord(LuceneMorphology morphology, String word) {
        boolean isPredlog = morphology.getMorphInfo(word).toString().contains("ПРЕДЛ");
        boolean isChactica = morphology.getMorphInfo(word).toString().contains("ЧАСТ");
        boolean isSoiuz = morphology.getMorphInfo(word).toString().contains("СОЮЗ");
        if (isPredlog || isChactica || isSoiuz) {
            return false;
        }
        return true;
    }

    public String[] cleanHtml(String html) {
        return html.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }
}
