/**
 * BI风格
 *
 * Created by GUY on 2016/7/6.
 * @class FS.StyleSetting
 * @extends BI.Widget
 */
FS.StyleSetting = BI.inherit(BI.Widget, {

    _defaultConfig: function () {
        return BI.extend(FS.StyleSetting.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-fs-style-setting"
        });
    },

    _init: function () {
        FS.StyleSetting.superclass._init.apply(this, arguments);

        var style = this._createStyle();
        var color = this._createColor();
        var preview = this._createPreview();

        BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [style, color, preview]
        });
    },

    _createStyle: function () {
        var self = this;
        var label = BI.createWidget({
            type: "bi.label",
            height: 25,
            textAlign: "left",
            hgap: 10,
            text: BI.i18nText('BI-Total_Style')
        });

        this.style = BI.createWidget({
            type: "bi.text_value_combo",
            cls: "style-setting-combo",
            height: 25,
            text: BI.i18nText('BI-Common'),
            items: [{
                text: BI.i18nText('BI-Common'), value: 0
            }, {
                text: BI.i18nText('BI-Top_Down_Shade'), value: 1
            }]
        });
        this.style.on(BI.TextValueCombo.EVENT_CHANGE, function () {
            self._save();
            self._preview();
        });
        this.style.setValue(0);

        return BI.createWidget({
            type: "bi.horizontal_adapt",
            height: 40,
            columnSize: [135, 260, ''],
            items: [label, this.style, BI.createWidget()]
        })
    },

    _createColor: function () {
        var self = this;
        var label = BI.createWidget({
            type: "bi.label",
            height: 25,
            textAlign: "left",
            hgap: 10,
            text: BI.i18nText('BI-Chart_Color')
        });

        this.color = BI.createWidget({
            type: "bi.text_value_combo",
            cls: "style-setting-combo",
            height: 25
        });
        this.color.on(BI.TextValueCombo.EVENT_CHANGE, function () {
            self._save();
            self._preview();
        });

        return BI.createWidget({
            type: "bi.horizontal_adapt",
            height: 40,
            columnSize: [135, 260, ''],
            items: [label, this.color, BI.createWidget()]
        })
    },

    _createPreview: function () {
        var self = this;
        var label = BI.createWidget({
            type: "bi.label",
            height: 25,
            textAlign: "left",
            hgap: 10,
            text: BI.i18nText('BI-Pre_View')
        });

        this.preview = BI.createWidget({
            type: "fs.chart_preview",
            width: 600,
            height: 400
        });

        return BI.createWidget({
            type: "bi.horizontal_adapt",
            verticalAlign: "top",
            columnSize: [135, 600, ''],
            items: [label, this.preview, BI.createWidget()]
        })
    },

    _preview: function () {
        this.preview.populate(this.data);
    },

    _save: function () {
        var chartStyle = this.style.getValue()[0] || 0;
        var defaultColor = this.color.getValue()[0];

        this.data.chartStyle = chartStyle;
        this.data.defaultColor = defaultColor;
        BI.requestAsync('fr_bi_base', 'set_config_setting', {
                chartStyle: chartStyle,
                defaultColor: defaultColor
            },
            function (res) {
                FR.Msg.toast(BI.i18nText("FS-Generic-Simple_Successfully"));
            }
        );
    },

    populate: function () {
        this.data = BI.requestSync('fr_bi_base', 'get_config_setting', null);
        this.color.populate(this.data.styleList);
        this.style.setValue(this.data.chartStyle || 0);
        if (BI.isKey(this.data.defaultColor)) {
            this.color.setValue(this.data.defaultColor);
        } else if (this.data.styleList.length > 0) {
            this.color.setValue(this.data.styleList[0].value);
        }
        this._preview();
    }
});
$.shortcut('fs.style_setting', FS.StyleSetting);