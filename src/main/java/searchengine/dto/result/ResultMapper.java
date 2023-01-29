package searchengine.dto.result;

import searchengine.services.Result;

public class ResultMapper {
    public static DtoResult dtoResult(Result result) {
        DtoResult dtoResult = new DtoResult();
        dtoResult.setSite(result.getPage().getSite().getUrl());
        dtoResult.setSiteName(result.getPage().getSite().getName());
        dtoResult.setUri(result.getPath());
        dtoResult.setTitle(result.getTitle());
        dtoResult.setSnippet(result.getSnippet());
        dtoResult.setRelevance(result.getRelevance());
        return dtoResult;
    }
}
