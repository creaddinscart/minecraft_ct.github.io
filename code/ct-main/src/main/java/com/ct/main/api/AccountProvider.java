package com.ct.main.api;

import java.util.Optional;
import java.util.function.Consumer;

public interface AccountProvider {
    void setCurrent(Account account);

    Optional<Account> current();

    void addListener(Consumer<Account> listener);
}
