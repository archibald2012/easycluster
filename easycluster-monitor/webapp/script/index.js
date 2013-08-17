Ext.namespace("org.easycluster.easycluster.monitor.index");

Ext.onReady(function() {
			Ext.QuickTips.init();
			var mainFrame = new Ext.Viewport({
						renderTo : "mainFrame",
						id : "mainframe",
						name : "mainFrame",
						layout : 'fit',
						listeners : {
							"resize" : function() {
								try {
									Ext.getCmp("north").setHeight((document.body.clientHeight - 165) / 2);
									Ext.getCmp("south").setHeight((document.body.clientHeight - 165) / 2);
									Ext.getCmp("east").setWidth((document.body.clientWidth - 400) / 2);
									Ext.getCmp("west").setWidth((document.body.clientWidth - 400) / 2);
									Ext.getCmp("mp").setWidth(document.body.clientWidth);
									Ext.getCmp("mp").setHeight(document.body.clientHeight);
								} catch (e) {
								}
							}
						}
					});
			mainFrame.add(new org.easycluster.easycluster.monitor.index.main());
			mainFrame.doLayout();
		});