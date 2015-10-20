
vdk.eventsHandler.addHandler(function(type, configuration) {
    if (type === "demands") {
        vdk.results.init();
        vdk.offers.init();
    }
});

function Results() {
}

Results.prototype = {
    init: function () {
        $("#facets").accordion({
            heightStyle: "content",
            collapsible: true
        });
        this.checkDifferences();
        this.zdrojNavButtons();
        this.doRange("#rokvydani_range", "#rokvydani_select", "rokvydani", true);
        this.doRange("#pocet_range", "#pocet_select", "pocet_exemplaru", false);
        this.parseDocs();
        this.clonePagination();
        this.renderOfferTitleDoc();
    },
    clonePagination: function(){
        $("#bottomPagination").append($("#results_header div.pagination").clone());
    },
    renderOfferTitleDoc: function(){
        $(".nabidka_ext").each(function () {
            var json = $(this).data('nabidka_ext');
            var elem = $(this);
            $.each(json, _.bind(function (key, val) {
                if (vdk.isLogged && val.pr_knihovna === vdk.user.id) {
                    //this.renderUserOffer(val);
                }else{
                    //elem.hide();
                }
                if(val.fields['245a']){
                    elem.text(val.fields['245a']);
                }
            }, this));
            
        });
    },
    parseDocs: function(){
        $(".res").each(function () {
            
            var res = $(this).attr('id');
            var code = $(jq(res) + ">input.code").val();
            var zaznam = $(jq(res) + ">input.identifier").val();
            $(this).find("table.tex").empty();
            $(jq(res) + " .ex").each(function () {
                vdk.results.parseDocExemplars($(this), code);
            });
            var $actions = $(this).find('.docactions');
            $actions.empty();
            $actions.append(vdk.actionOriginal(zaznam));
            if(vdk.isLogged){
                $actions.append(vdk.actionOffer(code));
                $actions.append(vdk.actionAddDemand(code));
            }
            $actions.append(vdk.actionCSV($(this).data("csv")));
            
        });
    },
    parseDocExemplars: function(div, code){
        var json = jQuery.parseJSON($(div).data("ex")).exemplare;
        var table = $(div).find("table.tex");
        if(json.length === 0){
            table.hide();
            return;
        }
        
        for(var i = 0; i<json.length; i++){
            var jsonex = json[i].ex;
            for(var j=0; j<jsonex.length; j++){
                $(table).append(this.renderDocExemplar(jsonex[j], json[i].id, json[i].zdroj, code));
            }
        }
            
        if ($(table).find("tr.more").length > 0) {
            var span =  $(table).find("thead>tr>th.actions>span");
            span.show();
            span.click(function (e) {
                $(table).find("tr.more").toggle();
            });
        } else {
           $(table).find("thead>tr>th.actions").css("float","none");
           $(table).find("thead>tr>th.actions").text(" ");
        }
            
    },
    renderDocExemplar: function(json, zaznam, zdroj, code){
        
        var sig = jsonElement(json, "signatura");
        if (sig.indexOf("SF") === 0) {
            return;
        }
                        
        var row = $('<tr class="" data-md5="' + json.md5 + '">');
        row.data("md5", json.md5);
        
        var icon = zdrojIcon(zdroj, json.isNKF);
        var filePath = "";
        if(json.hasOwnProperty("file")){
            '&path=' + json.file;
        }
        row.append('<td>' + icon +
                '<a style="float:right;" class="ui-icon ui-icon-extlink" target="_view" href="original?id=' + zaznam + filePath + '">view</a></td>');
        row.append("<td>" + jsonElement(json, "signatura") + "</td>");
        row.append("<td class=\"" + jsonElement(json, "status") + "\">" + jsonElement(json, "status", "status") + "</td>");
        row.append("<td>" + jsonElement(json, "dilchiKnih") + "</td>");
        row.append("<td>" + jsonElement(json, "svazek") + "</td>");
        row.append("<td>" + jsonElement(json, "cislo") + "</td>");
        row.append("<td>" + jsonElement(json, "rok") + "</td>");
        var checks = $('<td class="actions" style="width:95px;"></td>');
        if (vdk.isLogged !== null) {
            this.addAkce(row, checks, zaznam, zdroj, code, json.md5);
        } else {
            checks.append('<span style="margin-left:60px;">&nbsp;</span>');
        }
        row.append(checks);

//        exs.append(row);
//        if (exs.children().length > 3) {
//            $(row).addClass("more");
//        }
        return row;
    },
    addAkce: function (row, checks, zaznam, zdroj, code, exemplar) {
        if (vdk.exemplarBelongs(zdroj)) {
            checks.append(vdk.actionOffer(code, zaznam, exemplar));
        }
        if(vdk.demands.isUserDemand(code, zaznam, exemplar)){
            checks.append(vdk.actionRemoveDemand(code, zaznam, exemplar));
        }else{
            checks.append(vdk.actionAddDemand(code, zaznam, exemplar));
        }
    },
    
    doRange: function (id, sel, field, withMin) {
        var minv = $(id).data("min");
        var maxv = $(id).data("max");
        var min = 0;
        if (withMin) {
            min = minv;
        }
        $(id).slider({
            range: true,
            min: min,
            max: maxv,
            values: [minv, maxv],
            slide: function (event, ui) {
                $(sel + ">span.label").html("od " + ui.values[ 0 ] + " - do " + ui.values[ 1 ]);
                $(sel).data("from", ui.values[ 0 ]);
                $(sel).data("to", ui.values[ 1 ]);
            }
        });
        $(sel + ">span.go").button({
            icons: {
                primary: "ui-icon-arrowthick-1-e"
            },
            text: false
        });
        $(sel + ">span.go").click(function () {
            addRange(field, $(sel).data("from"), $(sel).data("to"));
        });
    },
    zdrojNavButtons: function () {
        $('#facets input.chci').button({
            icons: {
                primary: "ui-icon-check"
            },
            text: false
        });
        $('#facets input.nechci').button({
            icons: {
                primary: "ui-icon-cancel"
            },
            text: false
        }).change(function () {
            //saveWanted(id, identifier);
        });
    },
    checkDifferences: function () {
        //Vypnuto kvuli pridani NKF do zdroje
        //Nutno zpracovat primo z db
        return;
        $("li.res").each(function () {
            var res = $(this).attr('id');
            if ($(jq(res) + " input.numDocs").val() > 1) {
                var eq = true;
                for (var i = 0; i < $(jq(res) + " span.title").length - 1; i++) {
                    eq = ($(jq(res) + " span.title").eq(i).html() === $(jq(res) + " span.title").eq(i + 1).html()) && eq;
                }
                if (!eq) {
                    $(jq(res) + " div.diff").show();
                }
            }
        });
    },
}