package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.SpendingSummaryOutput;

import java.util.List;

public record SpendingSummaryResponse(double total, long count, List<CategorySummaryResponse> byCategory) {
    public static SpendingSummaryResponse from(SpendingSummaryOutput output) {
        return new SpendingSummaryResponse(
                output.total(),
                output.count(),
                output.byCategory().stream().map(CategorySummaryResponse::from).toList());
    }
}
