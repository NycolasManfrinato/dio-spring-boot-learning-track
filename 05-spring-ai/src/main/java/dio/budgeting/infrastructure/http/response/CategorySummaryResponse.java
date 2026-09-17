package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.CategorySummaryOutput;

public record CategorySummaryResponse(String category, double total, long count) {
    public static CategorySummaryResponse from(CategorySummaryOutput output) {
        return new CategorySummaryResponse(output.category(), output.total(), output.count());
    }
}
