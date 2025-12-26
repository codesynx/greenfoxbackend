package com.greenfox.backend.modules.ai.service;

import com.greenfox.backend.modules.resort.entity.Resort;
import java.util.ArrayList;
import java.util.List;

public class ResortSearchContext {
    private static final ThreadLocal<List<Resort>> CURRENT_RESORTS = ThreadLocal.withInitial(ArrayList::new);

    public static void addResorts(List<Resort> resorts) {
        if (resorts != null) {
            CURRENT_RESORTS.get().addAll(resorts);
        }
    }

    public static List<Resort> getResorts() {
        return new ArrayList<>(CURRENT_RESORTS.get());
    }

    public static void clear() {
        CURRENT_RESORTS.remove();
    }
}
