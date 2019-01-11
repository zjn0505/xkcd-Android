package xyz.jienan.xkcd.model;


import java.util.List;

import xyz.jienan.xkcd.model.persist.BoxManager;

public class ExtraModel {

    private static ExtraModel extraModel;

    private final BoxManager boxManager = BoxManager.getInstance();

    private ExtraModel() {
        // no public constructor
    }

    public static ExtraModel getInstance() {
        if (extraModel == null) {
            extraModel = new ExtraModel();
        }
        return extraModel;
    }

    public ExtraComics getExtra(int index) {
        return boxManager.getExtra(index);
    }

    public void update(List<ExtraComics> extraComics) {
        boxManager.saveExtras(extraComics);
    }

    public List<ExtraComics> getAll() {
        return boxManager.getExtraList();
    }
}
