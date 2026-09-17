package dio.budgeting.application;

import dio.budgeting.application.output.CategorySummaryOutput;
import dio.budgeting.application.output.SpendingSummaryOutput;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Novo tipo de consulta financeira: resume os gastos registrados, devolvendo
 * o total geral e o total por categoria.
 * <p>
 * Segue a mesma representação de valores usada em {@link dio.budgeting.application.output.TransactionOutput},
 * para manter consistência com o restante da API.
 */
@Service
public class SummarizeTransactionsUseCase {
    private final TransactionRepository transactionRepository;

    public SummarizeTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "summarize-transactions",
            description = "Resume os gastos financeiros registrados, retornando o total geral e o total por categoria")
    public SpendingSummaryOutput execute() {
        return summarize(transactionRepository.findAll());
    }

    private SpendingSummaryOutput summarize(List<Transaction> transactions) {
        var byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory))
                .entrySet()
                .stream()
                .map(entry -> new CategorySummaryOutput(
                        entry.getKey().name(),
                        toReais(entry.getValue().stream().mapToLong(Transaction::getAmount).sum()),
                        entry.getValue().size()))
                .sorted(Comparator.comparing(CategorySummaryOutput::category))
                .toList();

        long totalAmount = transactions.stream().mapToLong(Transaction::getAmount).sum();

        return new SpendingSummaryOutput(toReais(totalAmount), transactions.size(), byCategory);
    }

    private static double toReais(long amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
