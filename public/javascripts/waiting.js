/**
 * Created by rosexu on 15-09-19.
 */
var app = angular.module("app",[]).controller('AppController',function($scope, $http){
    $scope.users=[];
    $scope.getAllUsers = function() {
        $http.get('http://localhost:9000/get-all-users')
            .then(function (response){
                console.log(response.data)
                $scope.users = response.data;
            })
            .catch(function (){
                console.log("something went wrong with info retrieval");
            });
    }

    if ($(".players-list").data("is-from-join-game-page") === true) {
        $scope.getAllUsers()
    }
})