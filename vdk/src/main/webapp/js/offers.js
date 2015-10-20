
/* global vdk, _, dict */

function Offers() {
    this.loaded = false;
}

Offers.prototype = {
    init: function(){
        this.importDialog = null;
        this.retrieve();
    },
    openForm: function () {
        this.formDialog = $("<div/>", {title: vdk.translate('offers.addToOfferForm')});
        $("body").append(this.formDialog);
        this.formDialog.load("forms/add_to_offer.vm", _.bind(function () {
            this.formDialog.dialog({
                modal: true,
                width: "90%",
                height: 750,
                create: function(){
                    vdk.offers.formDialog.find('input.searcher').change(function(e){
                        vdk.offers.searchDoc($(this));
                    });
                }
            });
        }, this));    
    },
    openSearchForm: function () {
        this.searchFormDialog = $("<div/>", {title: vdk.translate('offers.searchToOfferForm')});
        $("body").append(this.searchFormDialog);
        this.searchFormDialog.load("forms/search_to_offer.vm", _.bind(function () {
            this.searchFormDialog.dialog({
                modal: true,
                width: "90%",
                height: 750,
                create: function(){
                    vdk.offers.searchFormDialog.find('input.searcher').change(function(e){
                        vdk.offers.searchDocs($(this));
                    });
                }
            });
        }, this));    
    },
    searchDoc: function(obj){
        var value = $(obj).val();
        if(value.length > 3){
            $.getJSON("search", {action: "BYFIELD", field: $(obj).data("field"), value: value}, _.bind(function(resp){
                if(resp.error){
                    $(obj).next().text("");
                }else{
                    if(resp.response.numFound > 0){
                        var doc = resp.response.docs[0];
                        var li = $("<div/>");
                        li.append(vdk.actionOffer(doc.code, doc.id[0]));
                        li.append('<div><b>' + doc.title[0] + '</b> ' + doc.author[0] + '</div>');
                        li.append('<div>' + doc.mistovydani[0] + doc.datumvydani[0] + ' (' + doc.isbn[0] + ')</div>');
                        $(obj).next().html(li);
                    }else{
                        $(obj).next().text("");
                    }
                }
            }, this));
            
        }
    },
    searchDocs: function(obj){
        var fq = [];
        this.searchFormDialog.find('input.searcher').each(function(){
            var value = $(this).val();
            if(value.length > 3){
                fq.push($(this).data("field") + ":" + value + "*");
            }
        });
        if(fq.length>0){
            $.getJSON("search", {action: "BYQUERY", q: "*:*", 'fq': fq}, _.bind(function(resp){
                if(resp.error){
                    $(obj).next().text("");
                }else{
                    var ul = this.searchFormDialog.find("ul.results");
                    ul.find("li").remove();
                    $.each(resp.response.docs, function(i, doc){
                        var li = $("<li/>");
                        li.append(vdk.actionOffer(doc.code, doc.id[0]));
                        var title = $('<div/>');
                        title.append('<b>' + doc.title[0] + '</b> ');
                        if(doc.hasOwnProperty('author')){
                            title.append(doc.author[0]);
                        }
                        li.append(title);
                        var ext = $('<div/>');
                        if(doc.hasOwnProperty('mistovydani')){
                            ext.append(doc.mistovydani[0]);
                        }
                        if(doc.hasOwnProperty('datumvydani')){
                            ext.append(doc.datumvydani[0]);
                        }
                        if(doc.hasOwnProperty('isbn')){
                            ext.append(' (' + doc.isbn[0] + ')');
                        }
                        li.append(ext);
                        ul.append(li);
                    });
                }
            }, this));
        }    
        
    },
    openImportDialog: function () {
        if(this.importDialog === null){
            this.importDialog = $("<div/>", {title: dict['select.offerImport']});
            $("body").append(this.importDialog);
            this.importDialog.load("forms/import_offer.vm", _.bind(function () {
                $("#importOfferFormId").val(this.activeid);
            }, this));
        }else{
            $("#importOfferFormId").val(this.activeid);
        }
        
        this.importDialog.dialog({
            modal: true,
            width: "90%",
            height: 750
        });
    },
    open: function () {
        if (!this.loaded) {
            this.dialog = $("<div/>", {title: dict['select.offer']});
            $("body").append(this.dialog);
            this.dialog.load("offers.vm", _.bind(function () {
                this.parseUser();
                var bs = [
                    {
                        class: "onopened",
                        text: vdk.translate('offers.searchToOfferForm'),
                        icon: "ui-icon-search",
                        click: function (e) {
                            vdk.offers.openSearchForm();
                        }
                    },
                    {
                        class: "onopened",
                        text: vdk.translate('offers.addToOfferForm'),
                        icon: "ui-icon-contact",
                        click: function (e) {
                            vdk.offers.openForm();
                        }
                    },
                    {
                        class: "onopened",
                        text: vdk.translate('offers.importToOfferForm'),
                        icon: "ui-icon-folder-open",
                        click: function (e) {
                            vdk.offers.openImportDialog();
                        }
                    },
                    {
                        text: "View report",
                        icon: "ui-icon-note",
                        click: function (e) {
                            window.open("reports/protocol.vm?id=" + vdk.offers.activeid, "report");
                        }
                    },
                    {
                        text: "Refresh",
                        icon: "ui-icon-refresh",
                        click: function (e) {
                            vdk.offers.getSelected();
                        }
                    }
                ];
                addButtons(bs, "#useroffer>div.buttons");
            }, this));
            this.loaded = true;
        }
        this.dialog.dialog({
            modal: true,
            width: "90%",
            height: 800,
            iconButtons: [
                {
                    text: "Refresh",
                    icon: "ui-icon-refresh",
                    click: function (e) {
                        vdk.offers.retrieve();
                    }
                },
                {
                    text: "Přídat nabídku",
                    icon: "ui-icon-plusthick",
                    click: function (e) {
                        vdk.offers.add();
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
    add: function () {
        var nazev = prompt("Nazev nabidky", "");
        if (nazev !== null && nazev !== "") {
            $.getJSON("db", {offerName: nazev, action: 'NEWOFFER'}, _.bind(function (data) {
                this.json[data.id] = data;
                this.renderUserOffer(data);

                this.setActive(data.id);
                $('#activeOffers').val(data.id);
            }, this));
        }
    },
    isWanted: function(zaznamoffer, knihovna){
        for(var i=0; i<this.wanted.length; i++){
            if(this.wanted[i].zaznamoffer === zaznamoffer
                    && this.wanted[i].knihovna === knihovna){
                return this.wanted[i].wanted;
            }
        }
        return null;
    },
    retrieve: function(){
        this.retrieveWanted();
    },
    retrieveWanted: function(){
        this.wanted = [];
        $.getJSON("db?action=GETWANTED", _.bind(function (json) {
            if(json.error){
                alert(vdk.translate(json.error));
            }else{
                this.wanted = json;
                this.retrieveOffers();
            }
        }, this));
    },
    retrieveOffers: function () {
        this.activeid = -1;
        $("#offers li.offer").remove();
        this.json = {};
        $.getJSON("db?action=GETOFFERS", _.bind(function (json) {
            this.json = json;
            $("#nav_nabidka li.offer").each(function () {
                var id = $(this).data("offer");
                var label = json[id].nazev + ' (' + json[id].knihovna + ')';
                $(this).find("a").text(label);
            });

            $(".offer>div").each(function () {
                var id = $(this).data("id");
                if (json.hasOwnProperty(id)) {
                    var val = json[id];
                    var text = '<label>' + val.nazev + " (" + val.knihovna + ")</label>";
                    $(this).html(text);
                }
            });
            this.parseUser();            
            
            $(".nabidka>div").each(function () {
                var offerId = $(this).data("offer");
                if (json.hasOwnProperty(offerId)) {
                    var val = json[offerId];
                    var expired = val.expired ? " expired" : "";
                    var label = $('<label class="' + expired + '">');
                    var offerJson = $(this).data("offer_ext")[offerId];
                    
                    
                    var of_datum = new Date(offerJson.datum);
                    var from_days = (vdk.user.priorita-1) * vdk.expirationDays;
                    var to_days = vdk.user.priorita * vdk.expirationDays;
                    
                    var from_datum = new Date(of_datum);
                    var to_datum = new Date(of_datum);
                    from_datum.setDate(of_datum.getDate() + from_days);
                    to_datum.setDate(of_datum.getDate() + to_days);

                    var text = val.knihovna + ' in offer ' + val.nazev +
                            ' (' + $.format.date(of_datum, 'dd.M.yy') + ' do ' + $.format.date(to_datum, 'dd.M.yy')+')';
                    var zaznamOffer = offerJson.zaznamOffer;
                    $(this).data("zaznamOffer", zaznamOffer);
                    $(this).attr("data-zaznamOffer", zaznamOffer);
                    var pr_knihovna = -1;
                    if(offerJson.hasOwnProperty('pr_knihovna')){
                        pr_knihovna = parseInt(offerJson.pr_knihovna);
                    }
                    

                    if ($(this).data("zaznam")) {
                        var zaznam = $(this).data("zaznam");
                        $("tr[data-zaznam~='" + zaznam + "']");
                    } else {
                        //je to nabidka zvenku, nemame zaznam.
                        
                    }
                    // pridame cenu jestli ma
                    if($(this).data("offer_ext")[offerId].fields.hasOwnProperty('cena')){
                        text += ' (' + $(this).data("offer_ext")[offerId].fields.cena + ')';
                    }
                    label.text(text);
                    $(this).append(label);
                    
                    if(vdk.isLogged && offerJson.hasOwnProperty('pr_knihovna') && vdk.user.id !== pr_knihovna){
                        $(this).hide();
                        $(this).removeClass("visible");
//                    }else if(vdk.isLogged && !offerJson.hasOwnProperty('pr_knihovna')){

                    }
                    if(vdk.isLogged && vdk.user.code !== val.knihovna){
                        var wanted = vdk.offers.isWanted(zaznamOffer, vdk.user.code);
                        if(wanted === null){
                            $(this).append(vdk.actionWant(zaznamOffer, true, true));
                            $(this).append(vdk.actionDontWant(zaznamOffer, true, true));
                            $(this).attr('title', dict['offer.want.unknown']);
                        }else if(wanted){
                            $(this).addClass('wanted');
                            $(this).append(vdk.actionWant(zaznamOffer, false, false));
                            $(this).append(vdk.actionDontWant(zaznamOffer, false, true));
                            $(this).attr('title', dict['chci.do.fondu']);
                        }else{
                            $(this).append(vdk.actionWant(zaznamOffer, false, true));
                            $(this).append(vdk.actionDontWant(zaznamOffer, false, false));
                            $(this).addClass('nowanted');
                            $(this).attr('title', dict['nechci.do.fondu']);
                        }
                    }
                    
                    if (!$(this).data("offer_ext")[offerId].hasOwnProperty('ex')) {
                        //je to nabidka na cely zaznam.
                    } else {
                        var ex = $(this).data("offer_ext")[offerId].ex;
                        var tr = $("tr[data-md5~='" + ex + "']");
                        tr.addClass("nabidka");
                        tr.find(".offerex, .demandexadd").remove();
                        if(vdk.isLogged && vdk.user.code !== val.knihovna){
                            tr.find("td.actions").append(vdk.actionWant(zaznamOffer));
                            tr.find("td.actions").append(vdk.actionDontWant(zaznamOffer));
                        }
                        $(this).mouseenter(function () {
                            tr.addClass("nabidka_over");
                        });
                        $(this).mouseleave(function () {
                            tr.removeClass("nabidka_over");
                        });

                    }
                }
                if($(this).parent().find("div.visible").length > 0){
                    $(this).parent().show();
                }
            });

        }, this));
    },
    parseUser: function () {
        $("#activeOffers>option").remove();
        $("#useroffers li.offer").remove();
        $.each(this.json, _.bind(function (key, val) {
            if (vdk.isLogged && val.knihovna === vdk.user.code) {
                this.renderUserOffer(val);
            }
        }, this));

        if (this.activeid === -1) {
            if ($("#useroffers li.offer").first()) {
                var offerid = $("#useroffers li.offer").first().data("offer");
                if (offerid !== null) {
                    this.clickUser(offerid);
                }
            }
        }

    },
    renderUserOffer: function (val) {
        var label = val.nazev;
        $("#activeOffers").append('<option value="' + val.id + '">' + label + '</option>');
        var li = $("<li/>", {"data-offer": val.id});
        li.addClass("offer");
        li.data("offer", val.id);
        var span1 = $('<span class="ui-icon" style="float:left;" />');
        if (val.expired) {
            span1.addClass("ui-icon-clock");
        } else {
            span1.addClass("ui-icon-check");
        }
        li.append(span1);
        var span = $('<span class="closed ui-icon" style="float:left;" />');
        if (val.closed) {
            span.addClass("ui-icon-locked");
            span.attr("title", "nabidka je zavrena");
        } else {
            span.addClass("ui-icon-unlocked");
            span.attr("title", "zavrit nabidku");
            span.click(_.bind(function (e) {
                this.close(val.id);
            }, this));
        }
        li.append(span);
        var a = $("<a/>");
        a.text(label);
        a.attr("href", "javascript:vdk.offers.clickUser('" + val.id + "');");
        li.append(a);
        $("#useroffers>ul").append(li);
    },
    renderDoc: function (val, closed) {
        var doc = $('<li/>', {class: 'offer', 'data-zaznamofferid': val.ZaznamOffer_id});
        doc.data("zaznamofferid", val.ZaznamOffer_id);

        var label = $('<div/>', {class: 'label'});
        if (val.hasOwnProperty('title')) {
            label.html(val.title + " (" + val.fields.comment + ")");
        } else {
            var html = "";
            if(val.hasOwnProperty('fields')){
                if (val.fields.hasOwnProperty('245a')) {
                    html += val.fields['245a'];
                }
                if (val.fields.hasOwnProperty('comment')) {
                    html += val.fields['comment'];
                }
            }
            label.html(html);
        }
        doc.append(label);
        if(!closed){
            var iconButtons = [{
                    text: "Remove",
                    icon: "ui-icon-close",
                    click: function (e) {
                        vdk.offers.removeDoc(val.ZaznamOffer_id);
                    }
                }];
            addButtons(iconButtons, doc);
        }
        if(val.hasOwnProperty('wanted')){
            $.each(val.wanted, function(key, wanted){
                doc.append('<span class="'+ (wanted.wanted ? '':'no') +'wanted">' + wanted.knihovna + "</span>" );
            });
        }
        $('#useroffer>ul').append(doc);
    },
    getSelected: function () {
        $('#useroffer>ul>li').remove();
        this.dialog.addClass("working");
        $.getJSON("db?action=GETOFFER&id=" + this.selectedid, _.bind(function (json) {
            this.active = json;
            this.selected = json;
            var closed = this.json[this.selectedid].closed;
            $.each(json, _.bind(function (key, val) {
                this.renderDoc(val, closed);

            }, this));
            this.dialog.removeClass("working");
        }, this));

    },
    setActive: function (id) {
        this.dialog.addClass("working");
        this.selectedid = id;
        if(!this.json[id].closed){
            this.activeid = id;
            $("#useroffers li.offer").removeClass("active");
            $("#useroffers li.offer").each(function () {
                if (id === $(this).data("offer")) {
                    $(this).addClass("active");
                    return;
                }
            });
            $("#importOfferForm input[name~='id']").val(this.activeid);
            $("#addToOfferForm input[name~='id']").val(this.activeid);
            $("#useroffer button.onopened").button("option", "disabled", false);
            $("#useroffer button.onopened").attr("disabled", false);
            
        }else{
            $("#useroffer button.onopened").button("option", "disabled", true);
            $("#useroffer button.onopened").attr("disabled", true);
        }
        $("#useroffers li.offer").removeClass("selected");
        $("#useroffers li.offer").each(function () {
            if (id === $(this).data("offer")) {
                $(this).addClass("selected");
                return;
            }
        });
        this.getSelected();
        $("#useroffer").show();
    },
    clickUser: function (id) {
        this.setActive(id);
        $('#activeOffers').val(id);
    },
    selectActive: function () {
        var id = $('#activeOffers').val();
        this.setActive(id);
    },
    remove: function (code, id, ex) {
        $.getJSON("db", {action: "REMOVEOFFER", docCode: code, zaznam: id, ex: ex}, function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
                return;
            }
            $.getJSON("index", {action: "REMOVEOFFER", docCode: code, zaznam: id, ex: ex}, _.bind(function (resp) {
                if (resp.error) {
                    alert("error ocurred: " + vdk.translate(resp.error));
                } else {
                    alert("Nabidka uspesne odstranena");
                }
            }, this));
        });

    },
    removeDoc: function (ZaznamOffer_id) {
        $.getJSON("db", {action: "REMOVEZAZNAMOFFER", ZaznamOffer_id: ZaznamOffer_id}, function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
            } else {
                $("#useroffer li.offer[data-zaznamofferid~='" + ZaznamOffer_id + "']").remove();
                ;
                alert(data.message);
            }
        });

    },
    close: function (id) {
        if(this.json[id].closed) return;
        new Confirm().open("opravdu chcete zavrit nabidku <b>"+this.json[id].nazev+"</b>?", _.bind(function () {
            $.post("db", {action: "CLOSEOFFER", id: id}, _.bind(function (resp) {
                if (resp.trim() === "1") {

                    //indexujeme
                    $.getJSON("index", {action: "INDEXOFFER", id: id}, _.bind(function (resp) {
                        if (resp.error) {
                            alert("error ocurred: " + vdk.translate(resp.error));
                        } else {

                            this.json[id].closed = true;
                            $("#useroffers li.offer").each(function () {
                                if ($(this).data("offer") === id) {
                                    $(this).find("span.closed").removeClass("ui-icon-unlocked").addClass("ui-icon-locked");
                                }
                            });

                            alert("Nabidka uspesne indexovana");
                        }
                    }, this));
                }

            }, this));
        }, this));

    },
    wantDoc: function (zaznam_offer, wanted, isnew) {
        var opts = {
            action: "REACTIONTOOFFER", 
            zaznam_offer: zaznam_offer, 
            wanted: wanted,
            isnew: isnew
        };
        $.getJSON("db", opts, function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
            } else {
                alert("Reakce uspesne zpracovana");
                var div = $("div[data-zaznamOffer~='" + zaznam_offer + "']");
                if(wanted){
                    div.addClass('wanted');
                    div.removeClass('nowanted');
                    div.find('button.wanteddoc').attr("disabled", "disabled");
                    div.find('button.nowanteddoc').removeAttr("disabled");
                }else{
                    div.addClass('nowanted');
                    div.removeClass('wanted');
                    div.find('button.nowanteddoc').attr("disabled", "disabled");
                    div.find('button.wanteddoc').removeAttr("disabled");
                }
            }

        });

    },
    addToActive: function (code, zaznam, ex) {
        new PriceAndComment().open(function(data){
            var opts = {
                action: "ADDDOCTOOFFER", 
                id: vdk.offers.activeid, 
                docCode: code, 
                comment: data.comment, 
                cena: data.price
            };
            if(zaznam){
                opts.zaznam = zaznam;
            }
            if(ex){
                opts.exemplar = ex;
            }
            $.getJSON("db", opts, function (data) {
                if (data.error) {
                    alert("error ocurred: " + vdk.translate(data.error));
                } else {
                    alert(data.message);
                }

            });
        });

    },
    addForm: function () {
        if (this.activeid === -1 || this.activeid === null) {
            alert("Neni zadna nabidka activni");
        } else {
            $("#addToOfferForm input[name~='id']").val(this.activeid);
            $.getJSON("db", $("#addToOfferForm").serialize(), function (data) {
                if (data.error) {
                    alert("error ocurred: " + vdk.translate(data.error));
                } else {
                    vdk.offers.renderDoc(data);
                    alert("Pridano!");
                }
            });
        }
    },
    import: function () {
        if (this.activeid === -1 || this.activeid === null) {
            alert("Neni zadna nabidka activni");
        } else {
            $("#importOfferFormId").val(this.activeid);
            $.getJSON("db", $("#importOfferForm").serialize(), function (data) {
                if (data.error) {
                    alert("error ocurred: " + vdk.translate(data.error));
                } else {
                    vdk.offers.renderDoc(data);
                    alert("Pridano!");
                }
            });
        }
    }
};


 