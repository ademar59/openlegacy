
var olControllers = angular.module('olControllers', []);

olControllers.controller('logonCtrl', ['$rootScope', '$state', '$scope','$http', '$location', function ($rootScope, $state, $scope, $http, $location) {    
    $scope.logon = function(username){
    	$rootScope.user = username;
    	$state.go("itemList");
    }	
}]);

olControllers.controller('HeaderCtrl', ['$rootScope', '$state','$scope','$http', '$location', function ($rootScope, $state, $scope, $http, $location) {    
	if ($rootScope.user != undefined) {
		$scope.username = $rootScope.user
	}
	
	$scope.logout = function(){
		delete $scope.username
		delete $rootScope.user
		$state.go("logon")
	}
	
}]);

olControllers.controller('FooterCtrl', ['$scope','$http', '$location', function ($scope, $http, $location) {    
    $scope.testtxt = "footer"
}]);

olControllers.controller('sidebarCtrl', ['$scope','$http', '$location', function ($scope, $http, $location) {    
    $scope.testtxt = "sidebar"
}]);

olControllers.controller('warehouseListCtrl', ['$scope','$http', '$location', '$stateParams', '$state', '$olData', function ($scope, $http, $location, $stateParams, $state, $olData) {    
    $olData.getWarehouses(function(data){
        console.log(JSON.stringify(data.model.entity.warehousesRecords));
        $scope.warehouses = data.model.entity.warehousesRecords
    });      
}]);

olControllers.controller('warehouseDetailsCtrl', ['$scope','$http', '$location', '$stateParams', '$state', '$olData', function ($scope, $http, $location, $stateParams, $state, $olData) {    
	$olData.getWarehouseDetails($stateParams.warehouseId,function(data){
		console.log(JSON.stringify(data.model.entity));
		$scope.warehouseDetails = data.model.entity;
	});
	
	
	$olData.getWarehouseTypes(function(data) {
		console.log(JSON.stringify(data.model.entity.warehouseTypesRecords));
		$scope.types = data.model.entity.warehouseTypesRecords;
		$scope.WhTypeClick = function(type) {
			$scope.warehouseDetails.warehouseType = type.type;			
		}
	});
}]);




olControllers.controller('itemListCtrl', ['$scope','$http', '$location', '$stateParams', '$state', '$olData', function ($scope, $http, $location, $stateParams, $state, $olData) {    
    $olData.getItems(function(data){
        console.log(JSON.stringify(data.model.entity.itemsRecords));
        $scope.items = data.model.entity.itemsRecords
    });      
}]);



olControllers.controller('itemDetailsCtrl', ['$scope','$http', '$location', '$stateParams', '$state', '$olData', function ($scope, $http, $location, $stateParams, $state, $olData) {    
	console.log(JSON.stringify($stateParams.itemId));
	
	$olData.getItemDetails($stateParams.itemId,function(data){
		console.log(JSON.stringify(data.model.entity));
		$scope.itemDetails = data.model.entity
	});  

    $olData.getShippingList(function(data){
        $scope.shippingList = data.shippingList;
    });  
	
	// sales chart options
    $scope.chartOptions = {
    	"salesChartUSD":{
            title: {
            	text: 'Monthly Sales Worldwide',
                x: -20 //center
            },
            subtitle: {
                text: 'in USD',
                x: -20
            },
            xAxis: {
                categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
            },
            yAxis: {
                title: {
                    text: 'Sales (USD)'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                valueSuffix: '$'
            },
            legend: {
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'middle',
                borderWidth: 0
            },
            series: [{
                name: 'Sales',
                data: [11234, 11762, 14526, 15662, 17653, 14050, 12293, 11532, 12773, 15762, 16273, 17839]
            }]    		    			
    	}, //end of sales chart in USD options
    	"salesChartItems":{
            title: {
            	text: 'Monthly Sales Worldwide',
                x: -20 //center
            },
            subtitle: {
                text: 'Number of itesm sold',
                x: -20
            },
            xAxis: {
                categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
            },
            yAxis: {
                title: {
                    text: 'Sales (Items)'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                //valueSuffix: '$'
            },
            colors: ['#ff4136'],           	         
            legend: {
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'middle',
                borderWidth: 0
            },
            series: [{
                name: 'Sales',
                data: [234, 244, 298, 325, 355, 332, 254, 240, 276, 311, 319, 332]
            }]    		    			
    	}, //end of sales chart in items options
    } //end of chartOptions 
    
    
    

}]);

