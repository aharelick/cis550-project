var Node = require('../models/Node.js');

var NUM_RESULTS = 5;

var getPaths = function(start, end) {
    start._depth   = 0;
    start._parents = [];
    queue = [ start ];
    seen  = [];

    while (queue.length > 0) {
        var node = queue.shift();
        seen.push(node);

        for (var i = 0; i < node.neighbors.length; i++) {
            adj = node.neighbors[i];
            if (!(adj.hasOwnProperty('_depth'))) {
                adj._depth   = node._depth + 1;
                adj._parents = [ node ];
                queue.push(adj);
            } else if (adj._depth === node._depth + 1) {
                adj._parents.push(node);
            }
        }
    }

    if (end.hasOwnProperty('_depth')) paths = generate(end);
    else                              paths = [ [ ] ];

    for (var i = 0; i < seen.length; i++) {
        delete seen[i]['_depth'];
        delete seen[i]['_parents'];
    }
    return paths;
}

var generate = function(end) {
    var paths = [];
    if (!(end.hasOwnProperty('_parents')) || end._parents.length === 0) {
        paths = [ [ end ] ];
    } 
    else {
        for (var i = 0; i < end._parents.length; i++) {
            var stubs = generate(end._parents[i]);
            for (var j = 0; j < stubs.length; j++) {
                stubs[j].push(end);
                paths.push(stubs[j]);
            }
        }
    }

    // is this going to be a problem?
    delete end['_depth'];
    delete end['_parents'];

    return paths;
}

var cartesian = function(nodes) {
    return {
        nodes: nodes,
        from: 0,
        fromnode: 0,
        to: 1,
        tonode: 0,
        next: function() {
            if (this.from >= nodes.length) return undefined;

            //console.log('(' + this.from + ', ' + this.fromnode + ')')
            //console.log('(' + this.to+ ', ' + this.tonode + ')')
            var a = nodes[this.from][this.fromnode];
            var b = nodes[this.to][this.tonode];

            do {
                this.tonode = (this.tonode + 1) % nodes[this.to].length;
                if (this.tonode === 0) {
                    this.to = (this.to + 1) % nodes.length;
                    if (this.to === 0) {
                        this.fromnode = (this.fromnode + 1) % nodes[this.from].length;
                        if (this.fromnode === 0) {
                            this.from = (this.from + 1);
                        }
                        this.to = this.from;
                    }
                }
            } while (this.to === this.from && this.from < nodes.length);
            
            return { 'from': a, 'to': b };
        }
    }
}

var topk = function(pairs, k) {
    var top = {
        list: [],
        _insert: function(path) {
            console.log(path);
            for (var i = 0; i < this.list.length; i++) {
                if (path.length < this.list[i].length) {
                    this.list.splice(i, 0, path);
                    return;
                }
            }
            this.list.push(path);
        }
    };

    var pair = pairs.next();
    while (pair) {
        var paths = getPaths(pair.from, pair.to);
        // console.log('(' + pair.from._id + ', ' + pair.to._id + ') ' + paths.length)
        while (paths.length > 0) {
            top._insert(paths.pop());
        }
        // console.log(top.list);
        pair = pairs.next();
    }
    
    console.log(top.list);
    return top.list.slice(0, k);
}


var search = function(query, callback) {
    var tokens = query.split(/[ ,\t\n\r]+/)
    var results = [];
    var finished = {
        init: function() {
            for (var i = 0; i < tokens.length; i++) {
                this[tokens[i]] = false;
            }
        },
        check: function() {
            for (var i = 0; i < tokens.length; i++) {
                if (!this[tokens[i]]) return false;
            }
            return true;
        }
    };

    finished.init();

    var assemble = function() {
        callback(topk(cartesian(nodes), NUM_RESULTS));
    }

    
    for (var i = 0; i < tokens.length; i++) {
        Node.find( { key: tokens[i] }, function(err, nodes) {
            if (err) console.log(err);
            else {
                finished[tokens[i]] = true;
                results.append(nodes);
                if (finished.check()) {
                    assemble();
                }
            }
        });
    }
}

module.exports = search;

