package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;


    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @DeleteMapping("/startIndexing")
    public void deleteAll() {
        indexingService.delete();
    }

    @GetMapping("/startIndexing")
    public HashMap<String, String> startIndexing() {
        HashMap<String, String> result = new HashMap<>();
        if (!indexingService.checkIndexing()) {
            indexingService.startService();
            result.put("result", "true");
            return result;
        }
        result.put("result", "false");
        result.put("error", "Индексация уже запущена");
        return result;
    }

    @GetMapping("/stopIndexing")
    public HashMap<String, String> stopIndexing() {
        HashMap<String, String> result = new HashMap<>();
        if (indexingService.checkIndexing()) {
            indexingService.stopService();
            result.put("result", "true");
            return result;
        }
        result.put("result", "false");
        result.put("error", "Индексация не запущена");
        return result;
    }
}
