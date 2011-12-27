package com.jetbrains.heroku.ui;

import com.heroku.api.Addon;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuAddonsWindow extends HerokuToolWindow {

    private AtomicInteger selectedRow;
    private AddonTableModel tableModel;

    @Override
    protected void setWindowInfo(ContentInfo contentInfo) {
        contentInfo.describe("Add-Ons","/debugger/threadSuspended.png","Service Add-Ons");
    }

    @Override
    protected JComponent createContentPane() {
        if (!herokuProjectService.isHerokuProject()) return null;
        final List<Addon> addons = load();
        tableModel = new AddonTableModel(herokuProjectService.getApplicationService().getAllAddons(), addons);
        selectedRow = new AtomicInteger(-1);
        return table(tableModel, selectedRow);
    }

    private List<Addon> load() {
        return herokuProjectService.getApplicationAddOns();
    }

    private void update() {
        tableModel.update(load());
    }

    @Override
    protected List<AnAction> createActions() {
        return Arrays.<AnAction>asList(
                new AnAction("Add Add-On", "", icon("/general/add.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Addon addon = tableModel.getAddOn(selectedRow.get());
                        if (addon==null || tableModel.isInstalled(addon)) return;
                        if (Messages.showYesNoDialog("Add the Add-On:"+addon.getName()+" "+addon.getPriceCents()+" "+addon.getPriceUnit(),"Add Add-On",Messages.getQuestionIcon())!=Messages.YES) return;
                        // ask confirmation
                        herokuProjectService.addAddon(addon);
                        HerokuAddonsWindow.this.update();
                    }
                },
                new AnAction("Remove Add-On", "", icon("/general/remove.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Addon addon = tableModel.getAddOn(selectedRow.get());
                        if (addon==null || !tableModel.isInstalled(addon)) return;
                        // ask confirmation
                        if (Messages.showYesNoDialog("Remove the Add-On:"+addon.getName(),"Remove Add-On",Messages.getQuestionIcon())!=Messages.YES) return;
                        herokuProjectService.removeAddon(addon);
                        HerokuAddonsWindow.this.update();
                    }
                },
                new AnAction("Show Documentation", "", icon("/xml/web_preview.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Addon addon = tableModel.getAddOn(selectedRow.get());
                        if (addon==null) return;
                        BrowserUtil.launchBrowser(addon.getUrl().toExternalForm());
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuAddonsWindow.this.update();
                    }
                }
        );
    }

    public HerokuAddonsWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }
}
