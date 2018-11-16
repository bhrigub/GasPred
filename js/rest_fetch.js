angular.module('demo', [])
.controller('fetch_rest', function($scope, $http) {
    $http.get('http://......').
        then(function(response) {
            $scope.greeting = response.data;
        });
});