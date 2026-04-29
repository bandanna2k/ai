package dnt.bank;

import dnt.bank.responses.ErrorResponse;
import dnt.common.Result;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static dnt.common.Result.failure;
import static dnt.common.Result.success;

public class BankService
{
    private final Map<Long, BigDecimal> balances;

    public BankService()
    {
        this.balances = new HashMap<>();
    }

    public Result<BigDecimal, ErrorResponse> getBalance(long accountId)
    {
        Optional<BigDecimal> maybeCurrentBalance = Optional.ofNullable(balances.get(accountId));
        return maybeCurrentBalance
                .map(Result::success)
                .orElse(failure(new ErrorResponse("ACCOUNT_NOT_FOUND", "Account with not found")))
                .mapError(error -> (ErrorResponse)error);
    }

    public Result<BigDecimal, ErrorResponse> submitWithdrawal(long accountId, BigDecimal amount)
    {
        Optional<BigDecimal> maybeCurrentBalance = Optional.ofNullable(balances.get(accountId));
        if (maybeCurrentBalance.isEmpty()) {
            return failure(new ErrorResponse("NOT_FOUND", "Account not found"));
        }

        BigDecimal currentBalance = maybeCurrentBalance.get();
        if (currentBalance.compareTo(amount) < 0) {
            return failure(new ErrorResponse("INSUFFICIENT_FUNDS", "Insufficient funds"));
        }

        BigDecimal newBalance = balances.put(accountId, currentBalance.subtract(amount));
        return success(newBalance);
    }

    public Result<BigDecimal, ErrorResponse> submitDeposit(long accountId, BigDecimal amount)
    {
        Optional<BigDecimal> maybeCurrentBalance = Optional.ofNullable(balances.get(accountId));
        if (maybeCurrentBalance.isEmpty()) {
            return failure(new ErrorResponse("NOT_FOUND", "Account not found"));
        }

        BigDecimal currentBalance = maybeCurrentBalance.get();
        BigDecimal newBalance = balances.put(accountId, currentBalance.add(amount));
        return success(newBalance);
    }
}
