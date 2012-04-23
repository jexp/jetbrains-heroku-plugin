package com.jetbrains.heroku.ui;

import com.heroku.api.Addon;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        final List<Addon> addons = load();
        tableModel = new AddonTableModel(herokuProjectService.getApplicationService().getAllAddons(), addons);
        selectedRow = new AtomicInteger(-1);
        return GuiUtil.table(tableModel, selectedRow);
    }

    private List<Addon> load() {
        if (!herokuProjectService.isHerokuProject()) return Collections.emptyList();
        return herokuProjectService.getApplicationAddOns();
    }

    public void doUpdate() {
        setEnabled(herokuProjectService.isHerokuProject());
        tableModel.update(load());
    }

    @Override
    protected List<AnAction> createActions() {
        return Arrays.<AnAction>asList(
                new AnAction("Add Add-On", "", icon("/general/add.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Addon addon = tableModel.getAddOn(selectedRow.get());
                        if (addon==null || tableModel.isInstalled(addon)) return;
                        final Price price = new Price(addon.getPriceCents(), addon.getPriceUnit());
                        if (Messages.showYesNoDialog("Add the Add-On: "+addon.getName()+" for "+price,"Add Add-On",Messages.getQuestionIcon())!=Messages.YES) return;
                        // ask confirmation
                        herokuProjectService.addAddon(addon);
                        HerokuAddonsWindow.this.doUpdate();
                    }
                },
                new AnAction("Remove Add-On", "", icon("/general/remove.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Addon addon = tableModel.getAddOn(selectedRow.get());
                        if (addon==null || !tableModel.isInstalled(addon)) return;
                        // ask confirmation
                        if (Messages.showYesNoDialog("Remove the Add-On:"+addon.getName(),"Remove Add-On",Messages.getQuestionIcon())!=Messages.YES) return;
                        herokuProjectService.removeAddon(addon);
                        HerokuAddonsWindow.this.doUpdate();
                    }
                },
                new AnAction("Show Documentation", "", icon("/xml/web_preview.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Addon addon = tableModel.getAddOn(selectedRow.get());
                        if (addon==null) return;
                        BrowserUtil.launchBrowser(addonUrl(addon));
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuAddonsWindow.this.doUpdate();
                    }
                }
        );
    }

    private static String addonUrl(Addon addon) {
        return "http://addons.heroku.com/"+ addonName(addon);
        // addon.getUrl();
    }

    private static String addonName(Addon addon) {
        final String name = addon.getName();
        if (name.contains(":")) return name.substring(0, name.indexOf(":"));
        return name;
    }

    public HerokuAddonsWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }
}
