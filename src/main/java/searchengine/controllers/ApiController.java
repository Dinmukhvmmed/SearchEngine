package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final IndexingService indexingService;
    private final SearchService searchService;

    public ApiController(IndexingService indexingService, SearchService searchService) {
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(indexingService.getStatistics());
    }

    @DeleteMapping("/deleteIndexing")
    public void deleteAll() {
        indexingService.delete();
    }

    @GetMapping("/startIndexing")
    public Map<String, Object> startIndexing() {
        if (!indexingService.checkIndexing()) {
            indexingService.startService();
            return Map.of("result", true);
        }
        return Map.of("result", false, "error", "Индексация уже запущена");
    }

    @GetMapping("/stopIndexing")
    public Map<String, Object> stopIndexing() {
        if (indexingService.checkIndexing()) {
            indexingService.stopService();
            return Map.of("result", true);
        }
        return Map.of("result", false, "error", "Индексация не запущена");
    }

    @PostMapping("/indexPage")
    public Map<String, Object> indexingOnePage(String url) {
        if (indexingService.checkURL(url)) {
            indexingService.indexingOnePage(url);
            return Map.of("result", true);
        }
        return Map.of("result", false, "error", "Данная страница находится за пределами сайтов,\n" +
                "указанных в конфигурационном файле");
    }

    @GetMapping("/search")
    public Map<String, Object> searching(String query, String site) {
        if (!query.isBlank()) {
            return Map.of("result", true,
                    "count", searchService.searchingAllSites(query).size(),
                    "data", searchService.searchingAllSites(query));
        }
        return Map.of("result", false, "error", "Задан пустой поисковый запрос");
    }
}
