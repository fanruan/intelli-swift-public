/**
 * @class BI.DashboardChartSetting
 * @extends BI.Widget
 * 仪表盘样式
 */
BI.DashboardChartSetting = BI.inherit(BI.AbstractChartSetting, {

    _constant: {
        SIMPLE_H_GAP2: 20,
        RADIO_WIDTH: 100,
        POINTER_SEGMENT_WIDTH: 150,
        PERCENTAGE_SEGMENT_WIDTH: 160
    },

    _defaultConfig: function () {
        return BI.extend(BI.DashboardChartSetting.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-charts-setting bi-dashboard-chart-setting"
        })
    },

    _init: function () {
        BI.DashboardChartSetting.superclass._init.apply(this, arguments);
        var self = this, constant = BI.AbstractChartSetting;

        //显示组件标题
        this.showTitle = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Show_Chart_Title"),
            cls: "attr-names",
            logic: {
                dynamic: true
            }
        });
        this.showTitle.on(BI.Controller.EVENT_CHANGE, function () {
            self.widgetTitle.setVisible(this.isSelected());
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        //组件标题
        this.title = BI.createWidget({
            type: "bi.sign_editor",
            cls: "title-input",
            width: 120
        });

        this.title.on(BI.SignEditor.EVENT_CHANGE, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE)
        });

        //详细设置
        this.titleDetailSettting = BI.createWidget({
            type: "bi.show_title_detailed_setting_combo"
        });

        this.titleDetailSettting.on(BI.ShowTitleDetailedSettingCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE)
        });

        this.widgetTitle = BI.createWidget({
            type: "bi.left",
            items: [this.title, this.titleDetailSettting],
            hgap: constant.SIMPLE_H_GAP
        });

        var widgetTitle = BI.createWidget({
            type: "bi.left",
            cls: "single-line-settings",
            items: BI.createItems([{
                type: "bi.label",
                text: BI.i18nText("BI-Component_Widget"),
                cls: "line-title",
            }, {
                type: "bi.label",
                text: BI.i18nText("BI-Title"),
                cls: "line-title",
                lgap: 38
            }, {
                type: "bi.vertical_adapt",
                items: [this.showTitle]
            }, {
                type: "bi.vertical_adapt",
                items: [this.widgetTitle]
            }], {
                height: constant.SINGLE_LINE_HEIGHT
            }),
            hgap: constant.SIMPLE_H_GAP
        });

        this.chartTypeGroup = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems(BICst.DASHBOARD_CHART_STYLE_GROUP, {
                type: "bi.icon_button",
                extraCls: "chart-style-font",
                width: constant.BUTTON_WIDTH,
                height: constant.BUTTON_HEIGHT,
                iconWidth: constant.ICON_WIDTH,
                iconHeight: constant.ICON_HEIGHT
            }),
            layouts: [{
                type: "bi.vertical_adapt",
                height: constant.SINGLE_LINE_HEIGHT
            }]
        });
        this.chartTypeGroup.on(BI.ButtonGroup.EVENT_CHANGE, function (v) {
            self._showPointer(v);
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        //组件背景
        this.widgetBackground = BI.createWidget({
            type: "bi.global_style_index_background"
        });
        this.widgetBackground.on(BI.GlobalStyleIndexBackground.EVENT_CHANGE, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        var tableStyle = BI.createWidget({
            type: "bi.horizontal_adapt",
            columnSize: [80],
            cls: "single-line-settings",
            items: [{
                type: "bi.label",
                text: BI.i18nText("BI-Chart"),
                lgap: constant.SIMPLE_H_LGAP,
                textAlign: "left",
                cls: "line-title"
            }, {
                type: "bi.left",
                cls: "detail-style",
                items: BI.createItems([{
                    type: "bi.label",
                    text: BI.i18nText("BI-Type"),
                    cls: "attr-names",
                    lgap: this._constant.SIMPLE_H_GAP2
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.chartTypeGroup],
                    lgap: constant.SIMPLE_H_GAP
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Widget_Background_Colour"),
                    cls: "line-title",
                    lgap: constant.SIMPLE_H_GAP
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.widgetBackground],
                    lgap: constant.SIMPLE_H_GAP
                }], {
                    height: constant.SINGLE_LINE_HEIGHT
                })
            }]
        });

        //数量级和单位
        this.numberLevellY = BI.createWidget({
            type: "bi.segment",
            width: constant.NUMBER_LEVEL_SEGMENT_WIDTH,
            height: constant.BUTTON_HEIGHT,
            items: BICst.TARGET_STYLE_LEVEL
        });

        this.numberLevellY.on(BI.Segment.EVENT_CHANGE, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        this.LYUnit = BI.createWidget({
            type: "bi.sign_editor",
            width: constant.EDITOR_WIDTH,
            height: constant.EDITOR_HEIGHT,
            cls: "unit-input",
            watermark: BI.i18nText("BI-Custom_Input")
        });

        this.LYUnit.on(BI.SignEditor.EVENT_CONFIRM, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        this.lYAxisStyle = BI.createWidget({
            type: "bi.segment",
            width: constant.FORMAT_SEGMENT_WIDTH,
            height: constant.BUTTON_HEIGHT,
            items: BICst.TARGET_STYLE_FORMAT
        });

        this.lYAxisStyle.on(BI.Segment.EVENT_CHANGE, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        //千分符
        this.separators = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Separators"),
            width: 80
        });

        this.separators.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        //minScale
        this.minScale = BI.createWidget({
            type: "bi.sign_editor",
            width: constant.EDITOR_WIDTH,
            height: constant.EDITOR_HEIGHT,
            cls: "unit-input",
            watermark: BI.i18nText("BI-Default_Data"),
            validationChecker: function (v) {
                return self.maxScale.getValue() == '' ? true : BI.parseFloat(v) < BI.parseFloat(self.maxScale.getValue())
            }
        });

        this.minScale.on(BI.SignEditor.EVENT_CONFIRM, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE)
        });

        //maxScale
        this.maxScale = BI.createWidget({
            type: "bi.sign_editor",
            width: constant.EDITOR_WIDTH,
            height: constant.EDITOR_HEIGHT,
            cls: "unit-input",
            watermark: BI.i18nText("BI-Default_Data"),
            validationChecker: function (v) {
                return self.minScale.getValue() == '' ? true : BI.parseFloat(v) > BI.parseFloat(self.minScale.getValue())
            }
        });

        this.maxScale.on(BI.SignEditor.EVENT_CONFIRM, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE)
        });

        this.scale = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems(BICst.CHART_SCALE_SETTING, {
                type: "bi.single_select_radio_item",
                width: this._constant.RADIO_WIDTH,
                height: constant.BUTTON_HEIGHT
            }),
            layouts: [{
                type: "bi.horizontal_adapt",
                height: constant.BUTTON_HEIGHT
            }]
        });

        this.scale.on(BI.ButtonGroup.EVENT_CHANGE, function (v) {
            self._doClickButton(v);
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE)
        });

        //添加条件button
        this.addConditionButton = BI.createWidget({
            type: "bi.button",
            text: "+" + BI.i18nText("BI-Add_Condition"),
            height: constant.BUTTON_HEIGHT
        });

        this.addConditionButton.on(BI.Button.EVENT_CHANGE, function () {
            self.conditions.addItem();
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        this.conditions = BI.createWidget({
            type: "bi.chart_add_condition_group"
        });

        this.conditions.on(BI.ChartAddConditionGroup.EVENT_CHANGE, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        //percent
        this.percentage = BI.createWidget({
            type: "bi.segment",
            height: 28,
            width: this._constant.PERCENTAGE_SEGMENT_WIDTH,
            items: BICst.PERCENTAGE_SHOW
        });

        this.percentage.on(BI.Segment.EVENT_CHANGE, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE)
        });

        var labelPercentage = BI.createWidget({
            type: "bi.label",
            text: BI.i18nText("BI-Percentage"),
            height: constant.BUTTON_HEIGHT,
            cls: "attr-names"
        });

        this.textPercentage = BI.createWidget({
            type: "bi.left",
            items: [labelPercentage, this.percentage],
            lgap: constant.SIMPLE_H_GAP
        });

        //单指针，多指针
        this.pointer = BI.createWidget({
            type: "bi.segment",
            width: this._constant.POINTER_SEGMENT_WIDTH,
            height: constant.BUTTON_HEIGHT,
            items: BICst.POINTERS
        });

        this.pointer.on(BI.Segment.EVENT_CHANGE, function () {
            self.fireEvent((BI.DashboardChartSetting.EVENT_CHANGE));
        });

        this.dashboardScale = BI.createWidget({
            type: "bi.left",
            items: BI.createItems([{
                type: "bi.label",
                text: BI.i18nText("BI-Min_Scale"),
                lgap: constant.SIMPLE_H_GAP,
                cls: "attr-names"
            }, {
                type: "bi.vertical_adapt",
                items: [this.minScale]
            }, {
                type: "bi.label",
                text: BI.i18nText("BI-Max_Scale"),
                cls: "attr-names"
            }, {
                type: "bi.vertical_adapt",
                items: [this.maxScale]
            }, {
                type: "bi.vertical_adapt",
                items: [this.textPercentage]
            }], {
                height: constant.SINGLE_LINE_HEIGHT
            }),
            lgap: constant.SIMPLE_H_GAP
        });

        var lYAxis = BI.createWidget({
            type: "bi.horizontal_adapt",
            columnSize: [80],
            cls: "single-line-settings",
            verticalAlign: "top",
            items: [{
                type: "bi.label",
                textHeight: constant.SINGLE_LINE_HEIGHT,
                text: BI.i18nText("BI-Dashboard"),
                textAlign: "left",
                lgap: constant.SIMPLE_H_LGAP,
                cls: "line-title"
            }, {
                type: "bi.left",
                cls: "detail-style",
                items: BI.createItems([{
                    type: "bi.label",
                    text: BI.i18nText("BI-Number_of_pointers"),
                    textAlign: "left",
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.pointer]
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Num_Level"),
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.numberLevellY]
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Unit_Normal"),
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.LYUnit]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.separators]
                }, this.dashboardScale, {
                    type: "bi.vertical_adapt",
                    items: [this.scale]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.addConditionButton]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.conditions],
                    width: "100%",
                    height: ""
                }], {
                    height: constant.SINGLE_LINE_HEIGHT
                })
            }],
            lgap: constant.SIMPLE_H_GAP
        });

        //联动传递指标过滤条件
        this.transferFilter = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Bind_Target_Condition"),
            width: 170
        });
        this.transferFilter.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.DashboardChartSetting.EVENT_CHANGE);
        });

        var otherAttr = BI.createWidget({
            type: "bi.left_right_vertical_adapt",
            cls: "single-line-settings",
            items: {
                left: [{
                    type: "bi.label",
                    text: BI.i18nText("BI-Interactive_Attr"),
                    cls: "line-title"
                }, this.transferFilter]
            },
            height: constant.SINGLE_LINE_HEIGHT,
            lhgap: constant.SIMPLE_H_GAP
        });

        BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [widgetTitle, tableStyle, lYAxis, otherAttr],
            hgap: 10
        });

    },

    _doClickButton: function (v) {
        switch (v) {
            case BICst.SCALE_SETTING.AUTO:
                this.addConditionButton.setVisible(false);
                this.conditions.setVisible(false);
                break;
            case BICst.SCALE_SETTING.CUSTOM:
                this.addConditionButton.setVisible(true);
                this.conditions.setVisible(true);
                break;
        }
    },

    _showPointer: function (pictureType) {
        switch (pictureType) {
            case BICst.CHART_SHAPE.NORMAL:
            case BICst.CHART_SHAPE.HALF_DASHBOARD:
                this.pointer.setVisible(true);
                this.textPercentage.setVisible(false);
                break;
            case BICst.CHART_SHAPE.PERCENT_DASHBOARD:
            case BICst.CHART_SHAPE.PERCENT_SCALE_SLOT:
            case BICst.CHART_SHAPE.VERTICAL_TUBE:
            case BICst.CHART_SHAPE.HORIZONTAL_TUBE:
                this.pointer.setVisible(false);
                this.textPercentage.setVisible(true);
                break;
        }
    },

    populate: function () {
        var wId = this.options.wId;
        this.showTitle.setSelected(BI.Utils.getWSShowNameByID(wId));
        this.widgetTitle.setVisible(BI.Utils.getWSShowNameByID(wId));
        this.title.setValue(BI.Utils.getWidgetNameByID(wId));
        this.titleDetailSettting.setValue(BI.Utils.getWSTitleDetailSettingByID(wId));
        this.widgetBackground.setValue(BI.Utils.getWSWidgetBGByID(wId));
        this.transferFilter.setSelected(BI.Utils.getWSTransferFilterByID(wId));
        this.chartTypeGroup.setValue(BI.Utils.getWSChartDashboardTypeByID(wId));
        this.pointer.setValue(BI.Utils.getWSNumberOfPointerByID(wId));
        this._showPointer(BI.Utils.getWSChartDashboardTypeByID(wId));
        this.scale.setValue(BI.Utils.getWSScaleByID(wId));
        this._doClickButton(BI.Utils.getWSScaleByID(wId));
        this.conditions.setValue(BI.Utils.getWSDashboardStylesByID(wId));
        this.numberLevellY.setValue(BI.Utils.getWSDashboardNumLevelByID(wId));
        this.LYUnit.setValue(BI.Utils.getWSDashboardUnitByID(wId));
        this.minScale.setValue(BI.Utils.getWSMinScaleByID(wId));
        this.maxScale.setValue(BI.Utils.getWSMaxScaleByID(wId));
        this.percentage.setValue(BI.Utils.getWSShowPercentageByID(wId));
        this.separators.setSelected(BI.Utils.getWSNumberSeparatorsByID(wId));

    },

    getValue: function () {
        return {
            show_name: this.showTitle.isSelected(),
            widget_title: this.title.getValue(),
            title_detail: this.titleDetailSettting.getValue(),
            widget_bg: this.widgetBackground.getValue(),
            transfer_filter: this.transferFilter.isSelected(),
            chart_dashboard_type: this.chartTypeGroup.getValue()[0],
            number_of_pointer: this.pointer.getValue()[0],
            dashboard_number_level: this.numberLevellY.getValue()[0],
            auto_custom: this.scale.getValue()[0],
            style_conditions: this.conditions.getValue(),
            dashboard_unit: this.LYUnit.getValue(),
            min_scale: this.minScale.getValue(),
            max_scale: this.maxScale.getValue(),
            show_percentage: this.percentage.getValue()[0],
            num_separators: this.separators.isSelected()
        }
    },

    setValue: function (v) {
        this.showTitle.setSelected(v.show_name);
        this.title.setValue(v.widget_title);
        this.titleDetailSettting.setValue(v.title_detail);
        this.widgetBackground.setValue(v.widget_bg);
        this.transferFilter.setSelected(v.transfer_filter);
        this.chartTypeGroup.setValue(v.chart_dashboard_type);
        this.pointer.setValue(v.number_of_pointer);
        this.numberLevellY.setValue(v.dashboard_number_level);
        this.scale.setValue(v.auto_custom);
        this.conditions.setValue(v.style_conditions);
        this.LYUnit.setValue(v.dashboard_unit);
        this.minScale.setValue(v.min_scale);
        this.maxScale.setValue(v.max_scale);
        this.percentage.setValue(v.show_percentage);
        this.separators.setSelected(v.num_separators);
    }
});
BI.DashboardChartSetting.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.dashboard_chart_setting", BI.DashboardChartSetting);
