package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste unitário puro (sem contexto Spring e sem chamadas à OpenAI) para o
 * novo caso de uso de resumo de gastos, usando um repositório em memória.
 */
class SummarizeTransactionsUseCaseTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final SummarizeTransactionsUseCase useCase = new SummarizeTransactionsUseCase(repository);

    @Test
    void deveRetornarResumoVazioQuandoNaoHaTransacoes() {
        var summary = useCase.execute();

        assertEquals(0.0, summary.total());
        assertEquals(0, summary.count());
        assertEquals(0, summary.byCategory().size());
    }

    @Test
    void deveSomarTotalGeralETotalPorCategoria() {
        repository.save(new Transaction("Mercado", 5000, Category.GROCERIES));
        repository.save(new Transaction("Farmacia", 3000, Category.PHARMA));
        repository.save(new Transaction("Supermercado", 2000, Category.GROCERIES));

        var summary = useCase.execute();

        assertEquals(10000.0, summary.total());
        assertEquals(3, summary.count());
        assertEquals(2, summary.byCategory().size());

        var groceries = summary.byCategory().stream()
                .filter(c -> c.category().equals(Category.GROCERIES.name()))
                .findFirst()
                .orElseThrow();
        assertEquals(7000.0, groceries.total());
        assertEquals(2, groceries.count());
    }

    private static class InMemoryTransactionRepository implements TransactionRepository {
        private final List<Transaction> transactions = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            transactions.add(transaction);
            return transaction;
        }

        @Override
        public List<Transaction> findAllByCategory(Category category) {
            return transactions.stream().filter(t -> t.getCategory() == category).toList();
        }

        @Override
        public List<Transaction> findAll() {
            return List.copyOf(transactions);
        }
    }
}
