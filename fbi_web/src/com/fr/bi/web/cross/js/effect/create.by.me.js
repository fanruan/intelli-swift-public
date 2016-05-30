(function ($) {

    $.extend(FS, {

        createByMe: function ($tab, $content, entry) {
            entry.contentEl.empty();
            BI.requestAsync('fr_bi', 'get_folder_report_list', {}, function (list) {
                var templateManage = BI.createWidget({
                    type: "bi.template_manager",
                    element: entry.contentEl,
                    items: list
                });
                templateManage.on(BI.TemplateManager.EVENT_FOLDER_RENAME, function (id, name, pId, type) {
                    //重命名或者新建文件夹
                    BI.requestAsync("fr_bi", "template_folder_rename", {
                        id: id,
                        pId: pId,
                        name: name,
                        type: type
                    }, function () {
                    })
                });
                templateManage.on(BI.TemplateManager.EVENT_DELETE, function (id, type) {
                    //删除
                    BI.requestAsync("fr_bi", "template_folder_delete", {
                        id: id,
                        type: type
                    }, function () {
                    })
                });
                templateManage.on(BI.TemplateManager.EVENT_MOVE, function (selectedFolders, toFolder) {
                    //移动
                    BI.requestAsync("fr_bi", "template_folder_move", {
                        selected_folders: selectedFolders,
                        to_folder: toFolder
                    }, function () {
                    })
                });
                templateManage.on(BI.TemplateManager.EVENT_SHARE, function (reports, users) {
                    //分享
                    BI.requestAsync("fr_bi", "template_folder_share", {
                        reports: FR.encrypt(FR.jsonEncode(reports), "neilsx"),
                        users: FR.encrypt(FR.jsonEncode(users), "neilsx")
                    }, function () {
                    });
                })
            });
        }
    });

})(jQuery);