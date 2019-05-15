var app = new Vue({
    el: '#app',
    data: {
        pageNumber: "",
        rows: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        columns: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        shipInfo: [],
        salvoInfo: [],
        hitInfo: [],
        hitInfoEnemy: [],
        gameInfo: {},
        sinkInfoUser: [],
        sinkInfoEnemy: [],
        players: [],
        shipOrientation: "horizontal",
        selectedShip: [],
        placedShips: [],
        allShipLocations: [],
        allShipTypes: [],
        salvoLocations: [],
        allShipsPlaced: false,
        gameState: "",
        hover: false,
        hoverLocations: [],
        hoverClass: "",
    },

    created() {
        this.getPage();
        this.getData();
    },

    methods: {
        getData() {
            fetch("/api/game_view/" + this.pageNumber, {
                method: "GET",
                credentials: "include",
            }).then(function (response) {
                if (response.status == 401) {
                    app.showMessage("You're not logged in");
                }
                return response.json()
            }).then(function (data) {
                app.refreshPage();
                app.shipInfo = data.gameview.ships;
                app.gameInfo = data.gameview.games;
                app.salvoInfo = data.gameview.salvoes;
                app.hitInfo = data.gameview.enemy_salvoes;
                app.hitInfoEnemy = data.gameview.hit_on_enemy;
                app.sinkInfoUser = data.gameview.enemy_sunk_ships;
                app.sinkInfoEnemy = data.gameview.user_sunk_ships;
                app.gameState = data.gameview.game_state;
                app.getGamePlayers();
                app.getShipLocations();
                app.getSalvoLocations();
                app.getHitLocations();
                app.getSinkLocations();
            }).catch(function (error) {
                console.log(error)
            })
        },

        getPage(search) {
            var search = location.search;
            var obj = {};
            var reg = /(?:[?&]([^?&#=]+)(?:=([^&#]*))?)(?:#.*)?/g;

            search.replace(reg, function (match, param, val) {
                obj[decodeURIComponent(param)] = val === undefined ? "" : decodeURIComponent(val);
            });
            this.pageNumber = obj["gp"];
        },

        getShipLocations() {
            for (i = 0; i < app.shipInfo.length; i++) {
                for (j = 0; j < app.shipInfo[i].locations.length; j++) {
                    var locations = document.getElementById("U" + app.shipInfo[i].locations[j]);
                    locations.classList.add("ship_locations");
                };
            };
        },

        getSalvoLocations() {
            for (i = 0; i < app.salvoInfo.length; i++) {
                for (j = 0; j < app.salvoInfo[i].salvoLocations.length; j++) {
                    var locations = document.getElementById("E" + app.salvoInfo[i].salvoLocations[j]);
                    var turn = document.getElementById("E" + app.salvoInfo[i].salvoLocations[j]);
                    turn.innerHTML = app.salvoInfo[i].turn;
                    locations.classList.add("salvo_locations");
                    turn.classList.add("turns");
                };
            };
        },

        getHitLocations() {
            for (i = 0; i < app.hitInfo.length; i++) {
                for (j = 0; j < app.hitInfo[i].salvoLocations.length; j++) {
                    let locations = document.getElementById("U" + app.hitInfo[i].salvoLocations[j]);
                    if (locations.classList.contains("ship_locations")) {
                        locations.classList.add("hit_locations");
                        var turn = document.getElementById("U" + app.hitInfo[i].salvoLocations[j]);
                        turn.innerHTML = app.hitInfo[i].turn;
                        turn.classList.add("hit_locations");
                        turn.classList.add("turns");
                    }
                };
            };
            for (i = 0; i < app.hitInfoEnemy.length; i++) {
                let locations = document.getElementById("E" + app.hitInfoEnemy[i]);
                if (locations.classList.contains("salvo_locations")) {
                    locations.classList.add("hit_locations");
                }
            }
        },

        getSinkLocations() {
            for (i = 0; i < app.sinkInfoUser.length; i++) {
                for (j = 0; j < app.sinkInfoUser[i].locations.length; j++) {
                    let locations = document.getElementById("U" + app.sinkInfoUser[i].locations[j]);
                    if (locations.classList.contains("ship_locations")) {
                        locations.classList.add("sink_locations");
                    }
                }
            };
            for (i = 0; i < app.sinkInfoEnemy.length; i++) {
                for (j = 0; j < app.sinkInfoEnemy[i].locations.length; j++) {
                    let locations = document.getElementById("E" + app.sinkInfoEnemy[i].locations[j]);
                    if (locations.classList.contains("salvo_locations")) {
                        locations.classList.add("sink_locations");
                    }
                }
            }
        },

        getGamePlayers() {
            var empty = [];
            var gamePlayers = app.gameInfo.gamePlayers;
            for (i = 0; i < gamePlayers.length; i++) {
                if (gamePlayers[i].id == app.pageNumber) {
                    var playerOne = gamePlayers[i].player.email + " (you)";
                } else {
                    var playerTwo = gamePlayers[i].player.email;
                }
            }
            var object = {
                playerOne: playerOne,
                playerTwo: playerTwo,
            }
            empty.push(object);
            app.players = empty;
        },

        placeShips() {
            fetch("/api/games/players/" + app.pageNumber + "/ships", {
                credentials: 'include',
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(app.placedShips),
            }).then(r => {
                console.log(r.status);
                if (r.status == 201) {
                    app.getData();
                    app.allShipsPlaced = true;
                }
            }).catch(e => console.log(e));
        },

        orientationToggle(toggleState) {
            var toggleContainer = document.getElementById("toggle-container");
            if (toggleState) {
                toggleContainer.style.clipPath = 'inset(0 0 0 50%)';
                toggleContainer.style.backgroundColor = 'lightblue';
                app.shipOrientation = "vertical"
            } else {
                toggleContainer.style.clipPath = 'inset(0 50% 0 0)';
                toggleContainer.style.backgroundColor = 'lightblue';
                app.shipOrientation = "horizontal"
            }
        },

        selectShip(type) {
            let length;
            if (type == "aircraft-carrier") {
                length = 5;
            }
            if (type == "battleship") {
                length = 4;
            }
            if (type == "submarine") {
                length = 3;
            }
            if (type == "destroyer") {
                length = 3;
            }
            if (type == "patrol-boat") {
                length = 2;
            }
            var object = {
                type: type,
                length: length,
            }
            app.selectedShip = object;
        },

        selectACell(letter, number) {
            var type = app.selectedShip.type;
            var cell = document.getElementById("U" + letter + number);
            var array = [];
            var verticalLetters = [];
            var horizontalNumbers = [];
            let allLocations = [];

            if (!cell.classList.contains("ship_locations")) {
                if (app.shipOrientation == "horizontal") {
                    for (var i = 0; i < app.selectedShip.length; i++) {
                        var nextCell = parseInt(number) + i;
                        array.push(letter + nextCell);
                        horizontalNumbers.push(nextCell);
                    }
                    if (app.overlappingRule(array) == false && app.horizontalGridPlacement(horizontalNumbers) == true) {
                        var object = {
                            type: type,
                            locations: array,
                        }
                        this.placedShips.push(object);
                    } else if (this.placedShips.length != 0) {
                        array = [];
                        type = "";
                        app.showMessage("Can't place ship here");
                    } else if (app.overlappingRule(array) == true || app.horizontalGridPlacement(horizontalNumbers) == false) {
                        app.showMessage("Can't place ship here");
                        array = [];
                        type = "";
                    } else if (this.placedShips.length == 0) {
                        app.showMessage("Please select a ship");
                    }
                }
                if (app.shipOrientation == "vertical") {
                    for (var i = 0; i < app.selectedShip.length; i++) {
                        var nextCell = app.rows.indexOf(letter) + i;
                        var letters = app.rows[nextCell];
                        verticalLetters.push(nextCell);
                        array.push(letters + number);
                    }
                    if (app.overlappingRule(array) == false && app.verticalGridPlacement(verticalLetters) == true) {
                        var object = {
                            type: type,
                            locations: array,
                        }
                        this.placedShips.push(object);
                    } else if (this.placedShips.length != 0) {
                        array = [];
                        type = "";
                        app.showMessage("Can't place ship here");
                    } else if (app.overlappingRule(array) == true || app.verticalGridPlacement(verticalLetters) == false) {
                        array = [];
                        type = "";
                        app.showMessage("Can't place ship here");
                    } else if (this.placedShips.length == 0) {
                        app.showMessage("Please select a ship");
                    }
                }
                if (app.allShipTypes.includes(type)) {
                    app.removeShip(type);
                }            
                app.allShipTypes.push(type);
                for (let i = 0; i < this.placedShips.length; i++) {
                    let locations = this.placedShips[i].locations;
                    for (let j = 0; j < locations.length; j++) {
                        allLocations.push(locations[j]);
                    }
                }
                app.allShipLocations = allLocations;
            } else if (cell.classList.contains("ship_locations")) {
                app.showMessage("Can't place ship here");
            } else if (this.placedShips.length == 0) {
                app.showMessage("Please select a ship");
            }
        },

        overlappingRule(array) {
            let found = array.some(r => app.allShipLocations.includes(r));
            console.log(found);
            return found;
        },

        horizontalGridPlacement(array) {
            for (var i = 0; i < array.length; i++) {
                if ((11 - array[i]) < app.selectedShip.length) {
                    return false;
                }
                return true;
            }
        },

        verticalGridPlacement(array) {
            for (var i = 0; i < array.length; i++) {
                if ((10 - array[i]) < app.selectedShip.length) {
                    return false;
                }
                return true;
            }
        },
        
        removeShip(typeSelected) {
            for (i = 0; i < app.placedShips.length; i++) {
                var type = app.placedShips[i].type;
                if (type == typeSelected) {
                    app.allShipTypes.splice(i, 1);
                    app.placedShips.splice(i, 1);
                    break;
                }
            }
        },

        donePlacing() {
            if (app.placedShips.length == 5) {
                app.placeShips();
            } else app.showMessage("Please place all your ships before submitting");
        },

        addCellsClass(row, column) {
            let cellClass = "";
            if (this.allShipLocations.includes(row + column) && app.allShipsPlaced == false) {
                cellClass = "ship_locations";
            } else if (this.allShipLocations.includes(row + column) && app.allShipsPlaced == true) {
                cellClass = "all_ships_placed"
            }
            if (this.hover == true && this.hoverLocations.includes(row + column)) {
                cellClass = app.hoverClass;
            }
            return cellClass;
        },

        selectSalvoes(letter, number) {
            var cell = document.getElementById("E" + letter + number);
            if (!cell.classList.contains("salvo_locations") && app.salvoLocations.length <= 4) {
                cell.classList.add("salvo_locations");
                app.salvoLocations.push(letter + number);
            } else if (cell.classList.contains("salvo_locations")) {
                app.showMessage("Can't fire twice on same location");
            } else if (app.salvoLocations.length >= 4) {
                app.showMessage("Can't fire more than 5 salvoes per turn")
            }
        },

        fireSalvoes() {
            fetch("/api/games/players/" + app.pageNumber + "/salvoes", {
                credentials: 'include',
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    salvoLocations: app.salvoLocations
                }),
            }).then(r => {
                console.log(r.status);
                if (r.status == 201) {
                    app.getData();
                    app.salvoLocations = [];
                }
                if (r.status == 401) {
                    app.showMessage("Please log in.");
                }
            }).catch(e => console.log(e));
        },

        refreshPage() {
            setTimeout(function () {
                app.getData()
            }, 2000);
        },

        hoverShip(row, column) {
            var cell = document.getElementById("U" + row + column);
            var array = [];
            var verticalLetters = [];
            var horizontalNumbers = [];

            if (this.hover == true && this.selectedShip.length != 0) {
                if (app.shipOrientation == "horizontal") {
                    for (var i = 0; i < app.selectedShip.length; i++) {
                        var nextCell = parseInt(column) + i;
                        array.push(row + nextCell);
                        horizontalNumbers.push(nextCell);
                    }
                    if (app.overlappingRule(array) == false && app.horizontalGridPlacement(horizontalNumbers) == true) {
                        app.hoverLocations = array;
                        app.hoverClass = "hover_locations"
                    } else if (app.overlappingRule(array) == true || app.horizontalGridPlacement(horizontalNumbers) == false) {
                        app.hoverLocations = array;
                        app.hoverClass = "cannot_place_ship"
                    }
                }
                if (app.shipOrientation == "vertical") {
                    for (var i = 0; i < app.selectedShip.length; i++) {
                        var nextCell = app.rows.indexOf(row) + i;
                        var letters = app.rows[nextCell];
                        verticalLetters.push(nextCell);
                        array.push(letters + column);
                    }
                    if (app.overlappingRule(array) == false && app.verticalGridPlacement(verticalLetters) == true) {
                        app.hoverLocations = array;
                        app.hoverClass = "hover_locations"
                    } else if (app.overlappingRule(array) == true || app.verticalGridPlacement(verticalLetters) == false) {
                        app.hoverLocations = array;
                        app.hoverClass = "cannot_place_ship"
                    }
                }
            }
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
