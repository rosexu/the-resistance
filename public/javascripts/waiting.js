/**
 * Created by rosexu on 15-09-19.
 */
var app = angular.module("app",[]).controller('AppController',function($scope, $http){
    $scope.users=[
        {
            name:"Mahesh",
            description:"A geek",
            age:"22"
        },
        {
            name:"Ganesh",
            description:"A nerd",
            age:"25"
        },
        {
            name:"Ramesh",
            description:"A noob",
            age:"27"
        },
        {
            name:"Ketan",
            description:"A psychopath",
            age:"26"
        },
        {
            name:"Niraj",
            description:"Intellectual badass",
            age:"29"
        }
    ];

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

    $scope.getAllUsers()
})