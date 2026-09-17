package dio.budgeting.application.output;

import java.util.List;

public record SpendingSummaryOutput(double total, long count, List<CategorySummaryOutput> byCategory) {
}
