/* global vdk, _, dict */

/** 
 * Simple event handler used in application 
 * @constructor 
 */
function ApplicationEvents() {
}

ApplicationEvents.prototype = {
    handlerEnabled: true,
    enableHandler: function () {
        this.handlerEnabled = true;
    },
    disableHandler: function () {
        this.handlerEnabled = false;
    },
    /** contains event handlers*/
    handlers: [],
    /** 
     * Trigger event 
     * @method
     */
    trigger: function (type, data) {
        console.log("trigger event:" + type);
        if (!this.handlerEnabled) {
            console.log("handler disabled. Discarding event " + type);
            return;
        }

        $.each(this.handlers, function (idx, obj) {
            obj.apply(null, [type, data]);
        });
    },
    /** add new handler 
     *@method
     */
    addHandler: function (handler) {
        this.handlers.push(handler);
    },
    /** remove handler 
     * @method
     */
    removeHandler: function (handler) {
        /*
         var index = this.handlers.indexOf(handler);
         var nhandlers = [];
         if (index >=0)  {
         for (var i=0;i<index;i++) {
         nhandlers.push(this.handlers[i]);
         }
         for (var i=index+1;i<this.handlers.length;i++) {
         nhandlers.push(this.handlers[i]);
         }
         }
         this.handlers = nhandlers;
         */
    }
}

function VDK() {

    this.eventsHandler = new ApplicationEvents();

    this.user = null;
    this.expirationDays = 7;

    this.isLogged = false;
    this.zdrojUser = {
        'NKF': 'NKF',
        'UKF': 'UKF',
        'MZK': 'MZK',
        'VKOL': 'VKOLOAI'};
}
VDK.prototype = {
    exemplarBelongs: function(exZdroj){
        if(this.isLogged){
            if(this.user.code=== 'NKP'){
                return exZdroj==='NKF' || exZdroj==='UKF';
            }else{
                return  this.user.code === exZdroj;
            }
            
        }else{
            return false;
        }
    },
    actionOriginal: function (id) {
        var span = $('<button/>', {class: 'original', style: 'float:left;'});
        span.attr('title', 'nahlédnout originální metadata');
        span.click(function () {
            vdk.showOriginal(id);
        });
        var a = $('<a class="ui-icon ui-icon-extlink" >');
        a.attr('title', 'nahlédnout originální metadata');
        a.attr('href', 'javascript:void(0);');
        a.text('original');
        span.append(a);
        return span;
    },
    actionCSV: function (csv) {
        var span = $('<button/>', {class: 'original', style: 'float:left;'});
        span.attr('title', 'csv format');
        span.click(function () {
            vdk.showCSV(csv);
        });
        var a = $('<a class="ui-icon ui-icon-document" >');
        a.attr('title', 'csv format');
        //a.attr('href', 'javascript:vdk.showCSV("'+csv+'")');
        a.attr('href', 'javascript:void(0);');
        a.click(function () {
            vdk.showCSV(csv)
        });
        a.text('csv');
        span.append(a);
        return span;
    },
    actionOffer: function (code, id, ex) {
        var span = $('<button/>', {class: 'offerex', style: 'float:left;'});
        var a = $('<a class="ui-icon ui-icon-flag" >');
        span.attr('title', 'přidat do nabídky');
        a.attr('href', 'javascript:void(0)');
        span.click(function () {
            vdk.offers.addToActive(code, id, ex);
        });
        a.text('offer');
        span.append(a);
        return span;
    },
    actionAddDemand: function (code, id, ex) {
        var span = $('<button/>', {class: 'demandexadd', style: 'float:left;'});
        span.click(function () {
            vdk.demands.add(code, id, ex);
        });
        var a = $('<a class="ui-icon ui-icon-cart" >');
        span.attr('title', 'přidat do poptávky');
        a.attr('href', 'javascript:void(0)');
        a.text('demand');
        span.append(a);
        return span;
    },
    actionWant: function (zaznamOffer, isnew, enabled) {
        var span = $('<button/>', {class: 'wanteddoc', 'data-wanted': zaznamOffer, style: 'float:left;'});
        span.click(function () {
            vdk.offers.wantDoc(zaznamOffer, true, isnew);
        });
        if(!enabled){
            span.attr("disabled", "disabled");
        }
        var a = $('<a class="ui-icon ui-icon-star" >');
        span.attr('title', dict['offer.want'] + ' "' + dict['chci.do.fondu'] + '"');
        a.attr('href', 'javascript:void(0)');
        a.text('chci');
        span.append(a);
        return span;
    },
    actionDontWant: function (zaznamOffer, isnew, enabled) {
        var span = $('<button/>', {class: 'nowanteddoc', 'data-wanted': zaznamOffer, style: 'float:left;'});
        span.click(function () {
            vdk.offers.wantDoc(zaznamOffer, false, isnew);
        });

        var a = $('<a class="ui-icon ui-icon-cancel" >');
        span.attr('title', dict['offer.want'] + ' "' + dict['nechci.do.fondu'] + '"');
        if(!enabled){
            span.attr("disabled", "disabled");
        }
        a.attr('href', 'javascript:void(0)');
        a.text('chci');
        span.append(a);
        return span;
    },
    actionRemoveDemand: function (code, id, ex) {
        var span = $('<button/>', {class: 'demandexrem', style: 'float:left;'});
        span.click(function () {
            vdk.demands.remove(code, id, ex);
        });
        var a = $('<a class="ui-icon ui-icon-cancel" >');
        a.attr('title', 'odstranit z poptávky');
        a.attr('href', 'javascript:void(0)');
        a.text('demand');
        span.append(a);
        return span;
    },
    getUser: function () {
        $.getJSON("user.vm", _.bind(function (data) {
            this.user = data;
            this.isLogged = true;
        }, this));

    },
    setUser: function (data) {
            this.user = data;
            this.isLogged = true;

    },
    changeLanguage: function (lang) {
        $("#searchForm").append('<input name="language" value="' + lang + '" type="hidden" />');
        document.getElementById("searchForm").submit();
    },
    init: function () {
        //this.setUser();
        this.demands = new Demand();
        this.results = new Results();
        this.offers = new Offers();
        this.nabidka = new Nabidka();
        this.export = new Export();
        this.views = new Views();
        this.activeofferid = -1;
        this.getViews();
        autocompleteQ();
        //this.getOffers();
        $(document).tooltip({
            items: "div.diff, [title]",
            content: function () {

                var element = $(this);
                if (element.is("div.diff")) {
                    return $(this).children(" div.titles").html();
                }
                if (element.is("[title]")) {
                    return element.attr("title");
                }
            }
        });
        $(document).click(function(){
            $(".ui-tooltip-content").parents('div').remove();
        });
        

    },
    translate: function (key) {
        if (dict.hasOwnProperty(key)) {
            return dict[key];
        } else {
            return key;
        }
    },
    userOpts: function () {
        if (this.isLogged && this.zdrojUser[vdk.user.code]) {
            //Prihlaseny uzivatel je NKP, MZK nebo VKOL
            $(".offerdoc").hide();
            $(".offerex").show();
            $("demanddoc").hide();
        } else {
            $(".offerdoc").show();
            $(".offerex").hide();
            $("demanddoc").show();
        }
        ;
        //vdk.getUserOffers();
        $("li.res").each(function () {
            var id = $(this).find("input.groupid").val();
        });
    },
    selectView: function () {
        this.views.select();
    },
    getViews: function () {
        this.views.get();
    },
    saveView: function () {
        this.views.save();
    },
    openView: function () {
        this.views.open();
    },
    addToNabidka: function (id) {
        this.nabidka.add(id);
    },
    openNabidka: function () {
        this.nabidka.open();
    },
    openExport: function () {
        this.export.open();
    },
    showOriginal: function (id) {
        window.open("original?id=" + id, "original");
    },
    showCSV: function (csv) {
        if (!this.csv) {
            this.csvdialog = $('<div title="CSV format" class="csv1" ></div>');
            this.csv = $('<input style="width:100%;" type="text" value=""/>');
            this.csvdialog.append(this.csv);
            $("body").prepend(this.csvdialog);
            $(this.csv).focus(function () {
                $(this).select();
            });
        }
        this.csv.val(csv);
        this.csvdialog.dialog({modal: true, width: 700});
    },
    filterOnlyMatches: function () {
        var i = $("input.fq").length + 1;
        var input = '<input type="hidden" name="onlyMatches" id="onlyMatches" class="fq" value="yes" />';
        $("#searchForm").append(input);
        $("#offset").val("0");
        document.getElementById("searchForm").submit();
    }
};

function LoginDialog() {

}
LoginDialog.prototype = {
    _init: function () {


    }
};

function Views() {

}
Views.prototype = {
    _init: function () {


    },
    get: function () {
        var url = "db?action=LOADVIEWS";
        $.getJSON(url, function (data) {
            $("#saved_views").empty();
            $("#saved_views").append('<option value="">'+vdk.translate("select.view")+'</option>');
            $.each(data.views, function (i, item) {
                var option = $('<option />');
                option.val(item.query);
                option.text(item.nazev);
                if(item.query === window.location.search.substring(1)){
                    option.prop("selected", true);
                }
                $("#saved_views").append(option);
            });
        });
    },
    select: function () {
        var query = $("#saved_views").val();
        window.location.href = "?" + query;
    },
    open: function () {

        if (!this.loaded) {
            this.dialog = $("<div/>", {title: dict['select.view']});
            $("body").append(this.dialog);
            this.dialog.load("forms/view.vm");
            this.loaded = true;
        }
        this.dialog.dialog({modal: true, width: 400, height: 300});
    },
    save: function () {
        if ($("#viewName").val() === "") {
            return;
        }
        var url = "db?action=SAVEVIEW&" + $('#viewForm').serialize() + "&" + $('#searchForm').serialize();
        $.get(url, _.bind(function (data) {
            alert(data);
            this.get();
        }, this));
    }
};

function Nabidka() {
    this.loaded = false;
}
function Export() {
    this.loaded = false;
}

Export.prototype = {
    open: function () {
        var rows = prompt("Maxilmální počet dokumentů (příliš velké číslo může zahltit systém)", "40");
        if (rows !== null) {
            var start = prompt("Od kterého záznamu?", "0");
            if (start !== null) {
                var q = window.location.search;
                if (q === "") {
                    q = "?rows_export=" + rows + "&start_export=" + start + "&export=true";
                } else {
                    q += "&rows_export=" + rows + "&start_export=" + start + "&export=true";
                }
                var url = "csv/export.vm" + q;
                window.open(url, "export");
            }
        }
    }
};



var vdk = new VDK();