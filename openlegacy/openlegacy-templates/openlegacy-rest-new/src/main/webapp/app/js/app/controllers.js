(function() {

	'use strict';

	/* Controllers */
	
	var showPreloader = function() {
		$(".preloader").show();
		$(".content-wrapper").hide();
	};

	var hidePreloader = function() {
		if (allowHidePreloader = true) {			
			$(".preloader").hide(0);
			$(".content-wrapper").show(0);
		} else {
			allowHidePreloader = true;
		}		
	};

	var module = angular.module('controllers', ["ui.bootstrap"]);

	module = module.controller(
			'loginCtrl',
			function($scope, $olHttp, $rootScope, $state) {			
				if ($.cookie('loggedInUser') != undefined) {
					$state.go("menu");
				}
				$scope.login = function(username, password) {				
				var data = {"user":username,"password":password}
				$olHttp.post('login',data, 
							function() {
								var $expiration = new Date();
								var minutes = olConfig.expiration;
								$expiration.setTime($expiration.getTime() + minutes*60*1000)
								
								$.cookie('loggedInUser', username, {expires: $expiration, path: '/'});
								$rootScope.$broadcast("olApp:login:authorized", username);
								$state.go("menu");
							}
						);
				};		
			});
		module = module.controller(
			'logoffCtrl',
			function($scope, $olHttp, $rootScope) {				
				$olHttp.get('logoff', 
					function() {
						$rootScope.hidePreloader();
						$.removeCookie("loggedInUser", {path: '/'});
					}
				);
			});
		
		module = module.controller('headerCtrl',
			function ($rootScope, $scope, $http, $themeService, $olHttp, $modal, $state) {			
				$rootScope.$on("olApp:login:authorized", function(e, value) {
					$scope.username = value;
				});
				
				if ($.cookie('loggedInUser') != undefined) {
					$scope.username = $.cookie('loggedInUser');
				}
				
				
				$scope.logout = function(){
					$rootScope.allowHidePreloader = false;
					delete $scope.username
					$state.go("logoff");
				}
				
				$scope.changeTheme = function() {
					$themeService.changeTheme();
				};
				
				$scope.showMessages = false;
				$olHttp.get("messages", function(data){
					if (data.model != null && data.model != undefined && data.model != "") {						
						$scope.showMessages = true;
						
						$scope.messages = function() {
							var modalInstance = $modal.open({
								templateUrl: "views/messages.html",
								controller: "messagesModalCtrl",
								resolve:{
									messages: function() {
										return data.model;
									} 
								}
							});
						};
						
						if (olConfig.showSystemMessages) {				
							$scope.messages();
						}
					}		
				});
			});	
			
		module = module.controller('messagesModalCtrl', ['$scope', '$modalInstance','messages', function($scope, $modalInstance, messages) {			
			$scope.messages = messages;	
			$scope.close = function() {
				$modalInstance.close();
			};
			
		}]);
		
		module = module.controller(
			'menuCtrl',
			function($scope, flatMenu) {
				flatMenu(function(data) {				
					$scope.menuArray = data;
				});
			});
		
		module = module.controller('breadcrumbsCtrl', function($scope, $rootScope, $olHttp, $state) {			
			$rootScope.$on("olApp:breadcrumbs", function(e, value) {
				$scope.breadcrumbs = value;
			});
		});

		// template for all entities 
		<#if entitiesDefinitions??>
		<#list entitiesDefinitions as entityDefinition>	
		module = module.controller('${entityDefinition.entityName}Ctrl',
				function($scope, $olHttp,$stateParams, flatMenu, $themeService, $rootScope, $state) {
					$scope.noTargetScreenEntityAlert = function() {
						alert('No target entity specified for table action in table class @ScreenTableActions annotation');
					}; 
					$scope.read = function(){						  
					      $olHttp.get('${entityDefinition.entityName}/' <#if entityDefinition.keys?size &gt; 0>+ $stateParams.${entityDefinition.keys[0].name?replace(".", "_")}</#if> + "?children=false",
							function(data) {
					    	  	$rootScope.hidePreloader();
								$scope.model = data.model.entity;							
								$scope.baseUrl = olConfig.baseUrl;
								$rootScope.$broadcast("olApp:breadcrumbs", data.model.paths);
								
								$scope.doActionNoTargetEntity = function() {					
									$scope.model.actions=null;
									<#list entityDefinition.tableDefinitions?keys as key> 
									$scope.model.${entityDefinition.tableDefinitions[key].tableEntityName?uncap_first}sActions=null;
								    </#list>	
									
									$olHttp.post('${entityDefinition.entityName}/', $scope.model, function(data) {
										if (data.model.entityName == '${entityDefinition.entityName}'){
											$scope.model = data.model.entity;
											$rootScope.$broadcast("olApp:breadcrumbs", data.model.paths);
											$rootScope.hidePreloader();
										}
										else{
											$rootScope.allowHidePreloader = false;
											$state.go(data.model.entityName);
										}
									});
								};
								
								<#if (entityDefinition.childEntitiesDefinitions?size > 0)>
									var tabsContent = {};						
									tabsContent["${entityDefinition.entityName}"] = $scope.model;
									$scope.loadTab = function(entityName) {
										if (tabsContent[entityName] == null) { 
											$scope.model.actions=null;											
											$olHttp.get(entityName + '/' <#if (entityDefinition.keys?size > 0)>+ $stateParams.${entityDefinition.keys[0].name}</#if> + "?children=false", 
												function(data) {
													$rootScope.hidePreloader();
													$scope.model = data.model.entity;
													tabsContent[entityName] = data.model.entity; 
												});
										} else {
											$scope.model = tabsContent[entityName];
										}					
									};
								</#if>
							}							
						);
					};		

					flatMenu(function(data) {
						$scope.menuArray = data;
					});
					
					$scope.doAction = function(entityName, actionAlias) {						
						if (actionAlias == "") {
				    		var url = entityName + actionAlias;
				    	} else {
				    		var url = entityName + "?action=" + actionAlias;
				    	}  
						$olHttp.post(url,$scope.model, 
							function(data) {
								if (data.model.entityName == '${entityDefinition.entityName}'){
									$rootScope.hidePreloader();
									$scope.model = data.model.entity;								
								}
								else{					
									$rootScope.allowHidePreloader = false;
									$state.go(data.model.entityName);
								}
							}
						);
					};
					
					<#if (entityDefinition.sortedFields?size > 0)>
						<#list entityDefinition.sortedFields as field>
							<#if field.fieldTypeDefinition.typeName == 'fieldWithValues'>						
							$olHttp.get("${field.name?cap_first}s", function(data) {							
								$scope.${field.name}s = data.model.entity.${field.name}sRecords;							
								$scope.${field.name?cap_first}Click = function(${field.name}) {								
									$scope.model.${field.name} = ${field.name}.type;			
								}
							});
							</#if>						
						</#list>
					</#if>
					
					$scope.read();
				});
		
		</#list>
		</#if>
	
		/* Controller code place-holder start
		<#if entityName??>
		module = module.controller('${entityName}Ctrl',
					function($scope, $olHttp,$stateParams, flatMenu, $rootScope, $state) {
					$scope.noTargetScreenEntityAlert = function() {
						alert('No target entity specified for table action in table class @ScreenTableActions annotation');
					}; 
					$scope.read = function(){						
						$olHttp.get('${entityName}/'  <#if keys?size &gt; 0>+ $stateParams.${keys[0].name?replace(".", "_")}</#if> + "?children=false",
							function(data) {
								$rootScope.hidePreloader();						
								$scope.model = data.model.entity;							
								$scope.baseUrl = olConfig.baseUrl;
								$rootScope.$broadcast("olApp:breadcrumbs", data.model.paths);
								
								
								$scope.doActionNoTargetEntity = function(rowIndex, columnName, actionValue) {														
									$scope.model.actions=null;
									<#list entityDefinition.tableDefinitions?keys as key> 
									$scope.model.${entityDefinition.tableDefinitions[key].tableEntityName?uncap_first}sActions=null;
								    </#list>	
									
									$olHttp.post('${entityName}/', $scope.model, function(data) {
										if (data.model.entityName == '${entityName}'){
											$scope.model = data.model.entity;
											$rootScope.$broadcast("olApp:breadcrumbs", data.model.paths);
											$rootScope.hidePreloader();								
										}
										else{					
											$rootScope.allowHidePreloader = false;;
											$state.go(data.model.entityName);
										}
									});
								};
								
								<#if (childEntitiesDefinitions?size > 0)>
									var tabsContent = {};						
									tabsContent["${entityName}"] = $scope.model;
									$scope.loadTab = function(entityName) {
										if (tabsContent[entityName] == null) { 
											$scope.model.actions=null;											
											$olHttp.get(entityName + '/' <#if (keys?size > 0)>+ $stateParams.${keys[0].name}</#if> + "?children=false", 
												function(data) {													
													$scope.model = data.model.entity;
													tabsContent[entityName] = data.model.entity;
													$rootScope.hidePreloader(); 
												});
										} else {
											$scope.model = tabsContent[entityName];
										}					
									};
								</#if>
							}
						);
					};		

					flatMenu(function(data) {
						$scope.menuArray = data;
					});	
		
					
					$scope.doAction = function(entityName, actionAlias) {											
						if (actionAlias == "") {
				    		var url = entityName + actionAlias;
				    	} else {
				    		var url = entityName + "?action=" + actionAlias;
				    	}					
						$olHttp.post(url,$scope.model, 
							function(data) {
								if (data.model.entityName == '${entityName}'){
									$scope.model = data.model.entity;
									$rootScope.hidePreloader();								
								}
								else{					
									$rootScope.allowHidePreloader = false;
									$state.go(data.model.entityName);
								}
							}
						);
					};
					
					<#if (sortedFields?size > 0)>
						<#list sortedFields as field>
							<#if field.fieldTypeDefinition?? && field.fieldTypeDefinition.typeName == 'fieldWithValues'>						
							$olHttp.get("${field.name?cap_first}s", function(data) {							
								$scope.${field.name}s = data.model.entity.${field.name}sRecords;							
								$scope.${field.name?cap_first}Click = function(${field.name}) {								
									$scope.model.${field.name} = ${field.name}.type;			
								}
							});
							</#if>						
						</#list>
					</#if>				
					
					$scope.read();
				});
		</#if>
		Controller code place-holder end */

	/* Controller with JSONP code place-holder start
	<#if entityName??>
	module = module.controller('${entityName}Controller',
		function($scope, $location, $http,$stateParams,$templateCache) {
			<#list actions as action>
			$scope.${action.alias} = function(){
				$http({method: 'JSONP', url: olConfig.hostUrl + '/${entityName}/${action.programPath}/'<#if keys?size &gt; 0> + </#if><#list keys as key>$stateParams.${key.name?replace(".", "_")}<#if key_has_next>+</#if></#list> + '?callback=JSON_CALLBACK', cache: $templateCache}).
			      success(function(data, status) {
						$scope.${entityName?uncap_first} = data;
			      }).
			      error(function(data, status) {
			    	  alert("failed");
			    });
			};
			</#list>
			if ($stateParams.${keys[0].name?replace(".", "_")} != null){
				$scope.read();
			}
			
		});
	</#if>
 	Controller with JSONP code place-holder end */
})();
