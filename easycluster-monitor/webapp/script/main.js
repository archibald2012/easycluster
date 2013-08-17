Ext.namespace("org.easycluster.easycluster.monitor.index");

org.easycluster.easycluster.monitor.index.main = function() {
	var centerPanel;
	var ds;
	var condition = {};
	var timer = 30000;
	var task = {
		run : function() {
			ds.load(condition);
		},
		interval : timer
	}
	Ext.TaskMgr.start(task);
	var northPanel = new Ext.Panel({
				header : false,
				height : 0,
				region : "north",
				html : ""
			});

	var tree = new Ext.tree.TreePanel({
				title : "Server Navigation",
				// frame : true,
				border : false,
				autoScroll : true,
				root : new Ext.tree.TreeNode({
							id : 'root',
							text : 'Domains',
							draggable : false, // false默认设置，不能被拖拽
							expanded : true,// 设置菜单展开
							listeners : {
								"click" : function() {
									condition = {};
									ds.load(condition);
								}
							}
						})
			});

	var navigation = new Ext.Panel({
		width : 200,
		layout : "fit",
		region : "west",
		header : false,
		listeners : {
			"render" : function() {
				DWREngine.setAsync(false);
				showtree.showAll(function(r) {
					for (var i = 0; i < r.length; i++) {// domain
						var a = r[i].groups;
						var nodeDomain = new Ext.tree.TreeNode({
									text : r[i].name,
									expanded : true,
									listeners : {
										"click" : function(e) {
											condition = {
												params : {
													domain : e.attributes.text
												}
											};
											ds.load(condition);
										}
									}
								});
						tree.root.appendChild(nodeDomain);
						for (var s in a) {// groups
							if (s != undefined) {
								var nodeGroup = new Ext.tree.TreeNode({
									text : a[s].name,
									expanded : true,
									listeners : {
										"click" : function(e) {
											condition = {
												params : {
													domain : e.parentNode.attributes.text,
													group : e.attributes.text
												}
											};
											ds.load(condition);
										}
									}
								});
								nodeDomain.appendChild(nodeGroup);
								for (var d in a[s].servers) {// server
									if (d != undefined) {
										var b = a[s].servers[d];
										var nodeServer = new Ext.tree.TreeNode(
												{
													text :  b.serverStatus.ip
															+ ":"
															+ b.serverStatus.port,
													listeners : {
														"click" : function(e) {
														}
													}
												});
										nodeGroup.appendChild(nodeServer);
									}
								}
							}
						}
					}
				});
				DWREngine.setAsync(true);
			}
		}
	});

	navigation.add(tree);

	centerPanel = new Ext.Panel({
				header : false,
				border : false,
				region : "center",
				items : []
			});

	var recordType = new Ext.data.Record.create([{
				name : "serverStatus.service",
				type : "String"
			}, {
				name : "running",
				type : "bool"
			}, {
				name : 'serverStatus.ip',
				type : "String"
			}, {
				name : "serverStatus.port",
				type : "String"

			}, {
				name : 'serverStatus.version',
				type : "String"
			}, {
				name : 'starttime',
				type : "date"
			}, {
				name : 'heartbeatTime',
				type : "long"
			}, {
				name : "runningTime",
				type : "String"
			}

	]);

	var proxy = new Ext.data.DWRProxy(showtree.showServerSnapshot, true);

	ds = new Ext.data.Store({
				proxy : proxy,
				reader : new Ext.data.ListRangeReader({
							totalProperty : 'totalSize'
						}, recordType),
				remoteSort : false
			});

	ds.load(condition);

	var colModel = new Ext.grid.ColumnModel([new Ext.grid.RowNumberer({
						header : "NO.",
						width : 40
					}), {
				name : "serverStatus.service",
				header : "Service",
				sortable : true,
				dataIndex : "serverStatus.service",
				width : 80
			}, {
				name : "running",
				header : "Running",
				sortable : true,
				dataIndex : "running",
				width : 50,
				renderer : function(value) {
					if (value) {
						return "<div align='center'><img src='images/blue.gif' width='15' height='15'/></div>";
					} else {
						return "<div align='center'><img src='images/red.gif' width='15' height='15'/></div>";
					}
				}
			}, {
				name : 'serverStatus.ip',
				header : "IP",
				sortable : true,
				dataIndex : "serverStatus.ip"
			}, {
				name : 'serverStatus.port',
				header : "Port",
				sortable : true,
				width : 50,
				dataIndex : "serverStatus.port"
			}, {
				name : 'serverStatus.version',
				header : "Version",
				dataIndex : "serverStatus.version",
				sortable : true,
				width : 80
			}, {
				name : 'starttime',
				header : "Startup",
				dataIndex : "starttime",
				sortable : true,
				renderer : Ext.util.Format.dateRenderer('Y-m-d H:i:s')
			}, {
				name : 'heartbeatTime',
				header : "Last Heartbeat",
				dataIndex : "heartbeatTime",
				sortable : true,
				renderer : function(v) {
					var d = new Date(v);
					return d.pattern("yyyy-MM-dd HH:mm:ss");
				}
			}, {
				name : 'runningTime',
				header : "Running Time",
				dataIndex : "runningTime",
				sortable : true,
				renderer : function(v) {
					return v + " s";
				}
			}]);

	var timeStore = new Ext.data.SimpleStore({
				fields : ['id', 'time'],
				data : [["30000", "30s"], ["60000", "1m"], ["180000", "3m"],
						["300000", "5m"], ["600000", "10m"],
						["1800000", "30m"], ["3600000", "60m"]]
			});

	var timeLabel = new Ext.form.Label({
				text : "Auto Refresh:"
			});

	var timeComboBox = new Ext.form.ComboBox({
				editable : false,
				typeAhead : true,
				triggerAction : 'all',
				forceSelection : true,
				mode : 'local',
				store : timeStore,
				value : "30000",
				valueField : 'id',
				displayField : 'time',
				listeners : {
					"select" : function() {
						ds.load(condition);
						timer = timeComboBox.getValue();
					}
				}
			});
	var space = {
		xtype : 'tbspacer',
		width : 10
	};
	var grid = new Ext.grid.GridPanel({
				store : ds,
				border : false,
				enableColumnHide : false,
				region : "center",
				cm : colModel,
				autoSize : true,
				autoScroll : true,
				layout : "fit",
				height : document.body.clientHeight - 150,
				frame : true,
				viewConfig : {
					forceFit : true
				},
				loadMask : {
					msg : "Loading，please wait..."
				},
				tbar : [new Ext.Toolbar.Fill(), timeLabel, space, timeComboBox,
						space, new Ext.Button({
									text : "Refresh",
									handler : function() {
										ds.load(condition);
									}
								})]
			});

	centerPanel.add(grid);

	var downPanel = new Ext.Panel({
				header : false,
				border : false,
				region : "center",
				layout : "border",
				items : [navigation, centerPanel]
			});

	var mainPanel = new Ext.Panel({
				autoSize : true,
				header : false,
				border : false,
				height : document.body.clientHeight,
				layout : "border",
				items : [northPanel, downPanel]
			});

	return mainPanel;
}