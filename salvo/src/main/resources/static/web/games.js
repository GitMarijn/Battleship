  var app = new Vue({
      el: '#app',
      data: {
          data: {},
          games: [],
          gamesArray: [],
          leaderBoard: [],
          sortedLeaderBoard: [],
          show: false,
      },

      created() {
          this.getData();
      },

      methods: {
          getData() {
              fetch("/api/games", {
                  method: "GET",
                  credentials: "include",
              }).then(function (response) {
                  return response.json()
              }).then(function (data) {
                  app.data = data;
                  app.games = data.games;
                  if (app.data.player != null) {
                      app.show = true;
                  }
                  app.gamePlayerInfo();
              }).catch(function (error) {
                  console.log(error)
              });

              fetch("/api/scoreboard", {
                  method: "GET",
                  credentials: "include",
              }).then(function (response) {
                  return response.json()
              }).then(function (data) {
                  app.leaderBoard = data;
                  app.sortLeaderBoard();
              }).catch(function (error) {
                  console.log(error)
              });
          },

          gamePlayerInfo() {
              var empty = [];
              for (var i = 0; i < app.games.length; i++) {
                  for (var j = 0; j < app.games[i].gamePlayers.length; j++) {
                      var playerOne = app.games[i].gamePlayers[0].player.email;
                      var gpID1 = app.games[i].gamePlayers[0].id;
                      var date = new Date(app.games[i].created).toLocaleString();
                      var gameID = app.games[i].id;
                      if (app.games[i].gamePlayers[1] != null) {
                          var playerTwo = app.games[i].gamePlayers[1].player.email;
                          var gpID2 = app.games[i].gamePlayers[1].id;
                      } else {
                          var playerTwo = "-";
                      }
                  }
                  var object = {
                      playerOne: playerOne,
                      playerTwo: playerTwo,
                      date: date,
                      gpID1: gpID1,
                      gpID2: gpID2,
                      gameID: gameID,
                  }
                  empty.push(object);
              }
              app.gamesArray = empty;
          },

          sortLeaderBoard() {
              app.leaderBoard.sort(function (a, b) {
                  return b["total"] - a["total"] || b["wins"] - a["wins"] || b["ties"] - a["ties"];
              });
          },

          login() {
              fetch("/api/login", {
                  credentials: 'include',
                  method: 'POST',
                  headers: {
                      'Accept': 'application/json',
                      'Content-Type': 'application/x-www-form-urlencoded'
                  },
                  body: 'userName=' + document.getElementById("userName").value + '&password=' + document.getElementById("password").value,
              }).then(r => {
                  console.log(r.status);
                  if (r.status == 200) {
                      app.getData();
                  }
                  if (r.status == 401) {
                      app.showMessage("Username and password do not match.")
                  }
              }).catch(e => console.log(e));
          },

          logout() {
              fetch("/api/logout", {
                  credentials: 'include',
                  method: 'POST',
                  headers: {
                      'Accept': 'application/json',
                      'Content-Type': 'application/x-www-form-urlencoded'
                  },
              }).then(r => {
                  console.log(r)
                  if (r.status == 200) {
                      app.show = false
                  }
              }).catch(e => console.log(e))
          },

          signup() {
              fetch("/api/players", {
                  credentials: 'include',
                  method: 'POST',
                  headers: {
                      'Accept': 'application/json',
                      'Content-Type': 'application/x-www-form-urlencoded'
                  },
                  body: 'userName=' + document.getElementById("userName").value + '&password=' + document.getElementById("password").value,
              }).then(r => {
                  console.log(r);
                  if (r.status == 409) {
                      app.showMessage("Username already exists. Please try a different username.");
                  }
                  if (r.status == 201) {
                      app.login();
                  }
                  if (r.status == 403) {
                      app.showMessage("Please enter a username");
                  }
              }).catch(e => console.log(e));
          },

          newGame() {
              fetch("/api/games", {
                      credentials: 'include',
                      method: 'POST',
                      headers: {
                          'Accept': 'application/json',
                          'Content-Type': 'application/x-www-form-urlencoded'
                      },
                  }).then(r => {
                      console.log(r);
                      if (r.status == 401) {
                          app.showMessage("Please log in")
                      }
                      if (r.status == 201) {
                          return r.json();
                      }
                  })
                  .then(json => location.href = "game.html?gp=" + json.gpid)
                  .catch(e => console.log(e));
          },

          joinGame() {
              fetch("/api/game/" + document.getElementById("join").getAttribute("data-gameID") + "/players", {
                      credentials: 'include',
                      method: 'POST',
                      headers: {
                          'Accept': 'application/json',
                          'Content-Type': 'application/x-www-form-urlencoded'
                      },
                  }).then(r => {
                      console.log(r);
                      if (r.status == 401) {
                          app.showMessage("Please log in")
                      }
                      if (r.status == 201) {
                          return r.json()
                      }
                  })
                  .then(json => location.href = "game.html?gp=" + json.gpid)
                  .catch(e => console.log(e));
          },
          
          showMessage(content) {
            var snackBar = document.getElementById("snackbar");
            snackBar.className = "show";
            snackBar.innerHTML = content;
            setTimeout(function () {
                snackBar.className = snackBar.className.replace("show", "");
            }, 3000);
        },
          
      },
  });
