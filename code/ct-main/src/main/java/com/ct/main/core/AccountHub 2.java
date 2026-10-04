package com.ct.main.core;

import com.ct.main.api.Account;
import com.ct.main.api.AccountProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class AccountHub implements AccountProvider {
    private final List<Consumer<Account>> listeners = new ArrayList<>();
    private Account current;

    @Override
    public void setCurrent(Account account) {
        current = account;
        for (Consumer<Account> listener : snapshot()) {
            listener.accept(account);
        }
    }

    @Override
    public Optional<Account> current() {
        return Optional.ofNullable(current);
    }

    @Override
    public void addListener(Consumer<Account> listener) {
        synchronized (this) {
            listeners.add(listener);
        }
    }

    private List<Consumer<Account>> snapshot() {
        synchronized (this) {
            return List.copyOf(listeners);
        }
    }
}
