
function Demand() {
    this.loaded = false;
    this.dialog = null;
    this.activeid = -1;
    this.retreive();
}

Demand.prototype = {
    open: function () {
        if (this.dialog === null) {
            this.dialog = $("<div/>", {title: dict['select.demand']});
            $("body").append(this.dialog);
            this.dialog.load("demands.vm", _.bind(function () {
                this.getUserDemands();
            }, this));
        }
        this.dialog.dialog({
            modal: true,
            width: 750,
            height: 600,
            iconButtons: [
                {
                    text: "Refresh",
                    icon: "ui-icon-refresh",
                    click: function (e) {
                        vdk.demands.retreive();
                    }
                },
                {
                    text: "Přídat poptavku",
                    icon: "ui-icon-plusthick",
                    click: function (e) {
                        vdk.demands.add();
                    }
                }
            ],
            create: function () {
                var $titlebar = $(this).parent().find(".ui-dialog-titlebar");
                $.each($(this).dialog('option', 'iconButtons'), function (i, v) {

                    var $button = $("<button/>").text(this.text),
                            right = $titlebar.find("[role='button']:last")
                            .css("right");

                    $button.button({
                        icons: {primary: this.icon},
                        text: false
                    }).addClass("ui-dialog-titlebar-close")
                            .css("right", (parseInt(right) + 22) + "px")
                            .click(this.click);

                    $titlebar.append($button);

                });
            }
        });
    },
    isDemand: function (code, zaznam, exemplar) {
        var retVal = false;
        $.each(this.json, function (key, val) {
            if (val.code === code && val.zaznam === zaznam && val.exemplar === exemplar) {
                retVal = true;
                return;
            }
        });
        return retVal;
    },
    isUserDemand: function (code, zaznam, exemplar) {
        var retVal = false;
        $.each(this.json, function (key, val) {
            if (vdk.isLogged && val.code === code &&
                    val.zaznam === zaznam &&
                    val.exemplar === exemplar &&
                    val.knihovna === vdk.user.code) {
                retVal = true;
                return;
            }
        });
        return retVal;
    },
    retreive: function () {
        $("#demands li.demand").remove();
        this.json = {};
        $.getJSON("db?action=GETDEMANDS", _.bind(function (json) {
            this.json = json;
            $.each(json, function (key, val) {
                var label = val.nazev + ' (' + val.knihovna + ' )';
                if (val.closed) {
                    //$("#demands").append('<li class="demand" data-demand="' + val.id + '"><a href="javascript:filterDemand(' + val.id + ');">' + label + '</a></li>');
                }
            });
            $(".demand>div").each(function () {
                var demandid = $(this).data("id");
                if (json.hasOwnProperty(demandid)) {
                    var val = json[demandid];
                    var text = '<label>' + val.nazev + " (" + val.knihovna + ")</label>";
                    $(this).html(text);
                }
            });
            this.getUserDemands();
            this.loaded = true;
            vdk.eventsHandler.trigger("demands", null);
        }, this));
    },
    getUserDemands: function () {
        $("#activeDemands>option").remove();
        $("#userdemands li.demand").remove();
        $.each(this.json, _.bind(function (key, val) {
            if (vdk.isLogged && val.knihovna === vdk.user.code) {
                this.renderDoc(val);
            }
        }, this));

    },
    isOffer: function (doc, code, zaznam, exemplar, knihovna) {
        $.getJSON('index', {action: 'ISOFFER', code: code, zaznam: zaznam, exemplar: exemplar}, _.bind(function (resp) {
            if(resp.hasOwnProperty('error')){
                alert("error ocurred: " + vdk.translate(resp.error));
            }else{
                doc.addClass("match");
//                var iconButtons = [{
//                        text: "Remove",
//                        icon: "ui-icon-close",
//                        click: function (e) {
//                            vdk.demands.remove(val.zaznamdemand_id, val.code, val.zaznam, val.exemplar);
//                        }
//                    }];
//                addButtons(iconButtons, doc);
            }
        }, this));
    },
    renderDoc: function (val) {
        var doc = $('<li/>', {class: 'demand', 'data-zaznamdemandid': val.zaznamdemand_id});
        doc.data("zaznamdemandid", val.zaznamdemand_id);

        var label = $('<div/>', {class: 'label'});
        var html = "";
        if (val.hasOwnProperty('title')) {
            html += val.title;
        } else {
            if (val.fields.hasOwnProperty('245a')) {
                html += val.fields['245a'];
            }
        }
        if (val.fields.hasOwnProperty('comment') && val.fields.comment !== "") {
            html += " (" + val.fields.comment + ")";
        }
        label.html(html);
        doc.append(label);
        var iconButtons = [{
                text: "Remove",
                icon: "ui-icon-close",
                click: function (e) {
                    vdk.demands.remove(val.zaznamdemand_id, val.code, val.zaznam, val.exemplar);
                }
            }];
        addButtons(iconButtons, doc);
        $('#userdemands>ul').append(doc);
    },
    remove: function (ZaznamDemand_id, docCode, zaznam, exemplar) {
        $.getJSON("db", {action: "REMOVEDEMAND", id: ZaznamDemand_id}, _.bind(function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
            } else {
                $.getJSON("index", {action: "REMOVEDEMAND", docCode: docCode, zaznam: zaznam, ex: exemplar}, _.bind(function (resp) {
                    if (resp.error) {
                        alert("error ocurred: " + vdk.translate(resp.error));
                    } else {
                        $("#userdemands li.demand[data-zaznamdemandid~='" + ZaznamDemand_id + "']").remove();
                        delete this.json[(ZaznamDemand_id + "")];
                        alert(data.message);
                    }
                }, this));

            }
        }, this));

    },
    add: function (code, id, ex) {
        var comment = prompt("Poznamka", "");
        if (comment === null)
            return;
        $.getJSON("db", {action: "ADDTODEMAND", docCode: code, zaznam: id, ex: ex, comment: comment}, function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
                return;
//            }
//            $.getJSON("index", {action: "ADDDEMAND", docCode: code, zaznam: id, ex: ex}, _.bind(function (resp) {
//                if (resp.error) {
//                    alert("error ocurred: " + vdk.translate(resp.error));
                } else {
                    alert("Poptavka uspesne indexovana");
                }
//            }, this));
        });

    },
    close: function (id) {
        $.post("db", {action: "CLOSEDEMAND", id: id}, _.bind(function (resp) {
            if (resp.trim() === "1") {

                //indexujeme
                $.getJSON("index", {action: "INDEXDEMAND", id: id}, _.bind(function (resp) {
                    if (resp.error) {
                        alert("error ocurred: " + vdk.translate(resp.error));
                    } else {

                        this.json[id].closed = true;
                        $("#userdemands li.demand").each(function () {
                            if ($(this).data("demand") === id) {
                                $(this).find("span.closed").removeClass("ui-icon-unlocked").addClass("ui-icon-locked");
                            }
                        });

                        alert("Poptavka uspesne indexovana");
                    }
                }, this));
            }

        }, this));
    }
};

