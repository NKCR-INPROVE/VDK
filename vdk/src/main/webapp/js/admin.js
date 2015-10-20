/* global vdk, _ */

function VDK_ADMIN() {
    this.eventsHandler = new ApplicationEvents();
    this._init();
}

VDK_ADMIN.prototype = {
    _init: function(){
        $('#admin>div.tabs').tabs();
        this.getRoles();
        this.getUsers();
        this.getJobs();
        this.getConf();
        //this.getSources();
    },
    getJobs: function(){
        var opts = {
                action: "GETJOBS"
            };
    
        $.getJSON("sched", opts, _.bind(function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
            } else {
                $("#jobs tbody>tr").remove();
                this.jobs = data;
                $.each(this.jobs, _.bind(function (i, val) {
                    var tr = $('<tr/>', {"data-jobKey": val.jobKey});
                    tr.data("jobKey", val.jobKey);
                    tr.attr("id", "job_"+val.jobKey);
                    tr.addClass(val.state);
                    
                    tr.append('<td>' + val.name + '</td>');
                    tr.append('<td>' + val.state + '</td>');
                    if(val.hasOwnProperty('nextFireTime')){
                        tr.append('<td>' + $.format.date(val.nextFireTime, 'dd.M.yy H:mm') + '</td>');
                    }else{
                        tr.append('<td> </td>');
                    }
                    
                    
                    if(val.hasOwnProperty('status')){
                        tr.append('<td>' + $.format.date(val.status['last_run'], 'dd.M.yy H:mm') + '<br/>' + val.status['last_message'] + '</td>');
                    }else{
                        tr.append('<td></td>');
                    }
                    if(val.state === "waiting"){
//                        var bt = $('<button/>');
//                        bt.text("start now");
//                        bt.click(_.bind(function(){
//                            this.startJob(val.jobKey);
//                        }, this));
//                        
//                        
//                        bt = $('<button/>');
//                        bt.text("reload config");
//                        bt.click(_.bind(function(){
//                            this.reloadJob(val.jobKey);
//                        }, this));
                        var td = $('<td/>');
                        var bs = [
                            {
                                text: vdk.translate('start now'),
                                icon: "ui-icon-play",
                                click: _.bind(function(){
                                    this.startJob(val.jobKey);
                                }, this)
                            }];
                        addButtons(bs, td);
                        //td.append(bt);
                        tr.append(td);
                    }
                    
                    if(val.state === "running"){
//                        var bt2 = $('<button/>');
//                        bt2.text("stop");
//                        bt2.click(_.bind(function(){
//                            this.stopJob(val.jobKey, val.name);
//                        }, this));
                        var td2 = $('<td/>');
                        
                        var bs2 = [
                            {
                                text: vdk.translate('stop'),
                                icon: "ui-icon-pause",
                                click: _.bind(function(){
                                    this.stopJob(val.jobKey, val.name);
                                }, this)
                            }];
                        addButtons(bs2, td2);
                        tr.append(td2);
                    }
                    
                    if(val.hasOwnProperty('conf')){
                        var cfgs = "";
                        var td= $("<td/>", {class: "settings"});
                        for(var key in val.conf.settings){
                            var div = $("<div/>");
                            var setting = val.conf.settings[key];
                            if(typeof(setting) === 'boolean'){
                                var chid = val.jobKey+"_"+key;
                                var check = $("<input/>", {type: "checkbox", class:"check", id: chid, name: key});
                                check.prop('checked', setting);
                                var label = $('<label/>', {for: chid});
                                label.text(key);
                                div.append(label);
                                div.append(check);
                            }else if(typeof(setting) === 'string'){
                                var chid = val.jobKey+"_"+key;
                                var input = $("<input/>", {type: "text", class:"text", id: chid, name: key});
                                input.val(setting);
                                var label = $('<label/>', {for: chid});
                                label.text(key);
                                div.append(label);
                                div.append(input);
                            }
                            cfgs += key + " -> " + val.conf.settings[key] + " ("  +  + ")";
                            td.append(div);
                        }
                        tr.append(td);
                    }else{
                        tr.append('<td></td>');
                    }
                    $("#jobs").append(tr);

                }, this));
            }

        }, this));
    },
    reloadJob: function(jobKey){
        
        var opts = {
                action: "RELOADJOB", key: jobKey
            };
        $.getJSON("sched", opts, _.bind(function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
            } else {
                alert(vdk.translate(data.message));
                this.getJobs();
                
            }

        }, this));
    },
    startJob: function(jobKey){
        var opts = {
                action: "STARTJOB", key: jobKey
            };
        var data = {};
        $(jq('job_' + jobKey) + " td.settings input.check").each(function(){
            data[$(this).attr("name")] = $(this).is(":checked");
        });
        $(jq('job_' + jobKey) + " td.settings input.text").each(function(){
            data[$(this).attr("name")] = $(this).val();
        });
        opts['data'] = JSON.stringify(data); 
        //alert(opts);
        //return;
        $.getJSON("sched", opts, _.bind(function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
            } else {
                alert(vdk.translate(data.message));
                this.getJobs();
                
            }

        }, this));
    },
    stopJob: function(jobKey, name){
        new Confirm().open("opravdu chcete zastavit proces <b>"+name+"</b>?", _.bind(function () {
            var opts = {
                    action: "STOPJOB", key: jobKey
                };
            $.getJSON("sched", opts, _.bind(function (data) {
                if (data.error) {
                    alert("error ocurred: " + vdk.translate(data.error));
                } else {
                    alert(vdk.translate(data.message));
                    this.getJobs();
                }

            }, this));
        
        }, this));
    },
    getRoles: function(){
        var opts = {
                action: "GETROLES"
            };
    
        $.getJSON("db", opts, function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
            } else {
                this.roles = data;
                
            }

        });
    },
    getSources: function(){
        var opts = {
                action: "GETSOURCES"
            };
    
        $.getJSON("db", opts, _.bind(function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
            } else {
                this.sources = data;
                $.each(this.sources, _.bind(function (key, val) {
                    var tr = $('<tr/>', {"data-code": key});
                    tr.append('<td>' + val.name + '</td>');
                    tr.append('<td>' + val.conf + '</td>');
                    
                    tr.append('<td><input class="cron" value="' + val.cron + '" /></td>');
                    var td = $('<td/>');
                    var bt = $('<button/>');
                        bt.text("save");
                        bt.click(_.bind(function(){
                            this.saveSource(key, val.conf);
                        }, this));
                        td.append(bt);
                    tr.append(td);
                    
                    tr.data("code", key);
                    
                    $("#sources").append(tr);

                }, this));
            }

        }, this));
    },
    saveSource: function(name, conf){
        var cron = $("#sources tr[data-code~='"+name+"']>td>input.cron").val();
        new Confirm().open(vdk.translate("user.comfirm.save") + " <b>" + name + "</b>?", _.bind(function () {
            var opts = {
                action: "SAVESOURCE", 
                name: name,
                cron: cron,
                conf: conf
                
            };
            $.getJSON("db", opts, _.bind(function (data) {
                if (data.error) {
                    alert("error ocurred: " + vdk.translate(data.error));
                } else {
                    this.getJobs();
                    alert(data.message);
                }

            }, this));

        }, this));
    },
    getConf: function(){
        var opts = {
                action: "GETCONF"
            };
    
        $.getJSON("db", opts, _.bind(function (data) {
            if (data.error) {
                //alert("error ocurred: " + vdk.translate(data.error));
                $("#tabs-conf>div.error").html(vdk.translate(data.error));
                $("#tabs-conf>div.error").show();
                $("#tabs-conf>div.content").hide();
            } else {
                $("#tabs-conf>div.error").hide();
                $("#tabs-conf>div.content").show();
                this.conf = data;
                $("#tabs-conf .exp").val(data.expirationDays);
                $("#tabs-conf .email").val(data["admin.email"]);
                $("#tabs-conf .emailBody").text(data["admin.email.offer.body"]);
            }

        }, this));
        
    },
    saveConf:function(){
        new Confirm().open(vdk.translate("conf.comfirm.save") + " <b>" + vdk.translate("conf.comfirm.save") + "</b>?", _.bind(function () {
            var opts = {
                action: "SAVECONF", 
                exp: $("#tabs-conf .exp").val(), 
                email: $("#tabs-conf .email").val()
                
            };
            $.getJSON("db", opts, _.bind(function (data) {
                if (data.error) {
                    alert("error ocurred: " + vdk.translate(data.error));
                } else {
                    alert("Conf saved !");
                }

            }, this));

        }, this));
    },
    getUsers: function(){
        var opts = {
                action: "GETUSERS"
            };
    
        $.getJSON("db", opts, _.bind(function (data) {
            if (data.error) {
                alert("error ocurred: " + vdk.translate(data.error));
            } else {
                this.users = data;
                $.each(this.users, _.bind(function (key, val) {
                    var li = $('<li/>', {class: 'link', "data-code": key});
                    li.text(val.name);
                    li.data("code", key);
                    li.click(_.bind(function(){
                        
                        this.selectUser(key);
                    }, this));
                    $("#users").append(li);

                }, this));
            }

        }, this));
    },
    selectUser: function(code){
        $("#users li").removeClass("selected");
        $("#users li[data-code~='"+code+"']").addClass("selected");
        this.selectedUser = this.users[code];
        $("#user .nazev").val(this.selectedUser.name);
        $("#user .priorita").val(this.selectedUser.priorita);
        $("#user .sigla").val(this.selectedUser.sigla);
        $("#user .adresa").val(this.selectedUser.adresa);
        $("#user .telefon").val(this.selectedUser.telefon);
        $("#user .email").val(this.selectedUser.email);
        $("#user ul.roles").empty();
        for(var i=0; i<this.selectedUser.roles.length; i++){
            $("#user ul.roles").append('<li>' + this.selectedUser.roles[i] + '</li>');
        }
        
    },
    addUser: function () {
        new Form().open([{name: "name", label: "Nazev"}, {name: "code", label: "Kod"}], function(data){
            var opts = {
                action: "ADDUSER", 
                code: data.code,
                name: data.name
            };
            $.getJSON("db", opts, function (data) {
                if (data.error) {
                    alert("error ocurred: " + vdk.translate(data.error));
                } else {
                    alert(data.message);
                }

            });
        });
    },
    saveUser: function () {
        new Confirm().open(vdk.translate("user.comfirm.save") + " <b>" + this.selectedUser.name + "</b>?", _.bind(function () {
            var opts = {
                action: "SAVEUSER", 
                code: this.selectedUser.code,
                name: $("#i_nazev").val(),
                priorita: $("#i_priorita").val(),
                email: $("#i_email").val(),
                sigla: $("#i_sigla").val(),
                adresa: $("#i_adresa").val(),
                telefon: $("#i_telefon").val()
                
            };
            $.getJSON("db", opts, _.bind(function (data) {
                if (data.error) {
                    alert("error ocurred: " + vdk.translate(data.error));
                } else {
                    this.users[this.selectedUser.code] = data;
                    alert("User saved !");
                }

            }, this));

        }, this));
    },
    deleteUser: function () {
        
        new Confirm().open(vdk.translate("user.comfirm.delete") + " <b>" + this.selectedUser.name + "</b>?", _.bind(function () {
            $.getJSON("db", {action: "DELETEUSER", code: this.selectedUser.code}, function (data) {
                if (data.error) {
                    alert("error ocurred: " + vdk.translate(data.error));
                } else {
                    alert(data.message);
                }

            });

        }, this));
    },
    addRole: function (code) {

    }
};