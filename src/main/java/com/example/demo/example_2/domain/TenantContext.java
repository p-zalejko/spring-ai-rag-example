package com.example.demo.example_2.domain;

/**
 * Simple ThreadLocal holder for the current tenant.
 * In a real app this would be populated from JWT claims
 * in a servlet filter before the request hits any service.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(String tenantId) {
        CURRENT.set(tenantId);
    }

    public static String current() {
        String tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant set in current thread context");
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT.remove();
    }
}