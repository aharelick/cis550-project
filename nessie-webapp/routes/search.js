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

var MAX_DEPTH = 20;
var search = function(start, end, callback) {
    var seen = [];
    var done = [];
    done[start._id] = false;

    start._depth   = Number.MAX_VALUE;
    start._parents = [];

    end._depth     = Number.MAX_VALUE;
    end._parents   = [];
    seen[end._id]  = end;

    dfs(start, end, null, seen, 0, done, function(node) {
        for (var key in done) {
            if (!done[key]) return;
        }
        callback(generate(end));
    });
}

var dfs = function(start, end, last, seen, depth, done, callback) {
    var print = start.key;
    if (last != null) {
        print = print + '\t' + last.key;
    }
    for (var i = 0; i < depth; i++) {
        print = '\t' + print;
    }
    console.log(print);
    seen[start._id] = start;
    if (depth == MAX_DEPTH) {
        done[start._id] = true;
        callback(start);
        return;
    }

    if (start._depth > depth) {
        start._depth = depth;
        if (last != null) start._parents = [ last ];
        else              start._parents = [ ];
    } else if (start._depth == depth) {
        start._parents.push(last);
    } else {
        done[start._id] = true;
        callback(start);
        return;
    }

    for (var i = 0; i < start.neighbors.length; i++) {
        done[start.neighbors[i]] = false;
        if (start.neighbors[i] in seen) {
            dfs(seen[start.neighbors[i]], end, start, seen, depth + 1, done, callback);
        } else {
            Node.findOne({ _id: start.neighbors[i] }, function(err, node) {
                node._depth   = Number.MAX_VALUE;
                node._parents = [];
                dfs(node, end, start, seen, depth + 1, done, callback);
            });
        }
    }

    done[start._id] = true;
    callback(start);
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

    console.log('generate ' + end);

    return paths;
}

var cartesian = function(nodes) {
    return {
        nodes: nodes,
        from: 0,
        fromnode: 0,
        to: 1,
        tonode: 0,
        size: function() {
            var out = 1;
            for (var i = 0; i < nodes.length; i++) {
                out = out * nodes[i].length;
            }
            return out;
        },
        next: function() {
            if (this.from >= nodes.length) {
                return undefined;
            }

            var a = this.nodes[this.from][this.fromnode];
            var b = this.nodes[this.to][this.tonode];

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

var topk = function(pairs, k, callback) {
    var top = {
        list: [],
        _insert: function(path) {
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
    var n    = 0;
    while (pair) {
        search(pair.from, pair.to, function(paths) {
            n++;
            while (paths.length > 0) {
                top._insert(paths.pop());
            }
            if (n == pairs.size()) {
                callback(top.list.slice(0, k));
            }
        });
        // console.log(pair.from._id + ' , ' + pair.to._id);
        pair = pairs.next();
    }
}


var searchengine = function(query, callback) {
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

    var assemble = function(token, results, finished, callback) {
        return function(err, nodes) {
            if (err) console.log(err);
            else {
                finished[token] = true;
                console.log('for ' + token + ' got ' + nodes);
                if (nodes.length > 0) results.push(nodes);
                if (finished.check()) {
                    if (results.length == 0) callback([]);
                    topk(cartesian(results), NUM_RESULTS, callback);
                }
            }
        }
    }

    
    for (var i = 0; i < tokens.length; i++) {
        Node.find({ key: tokens[i] }, assemble(tokens[i], results, finished, callback));
    }
}


var a = { _id: 'a' };
var b = { _id: 'b' };
var c = { _id: 'c' };
var d = { _id: 'd' };
var e = { _id: 'e' };

a.neighbors = [ b, c ];
b.neighbors = [ a, d ];
c.neighbors = [ a, d ];
d.neighbors = [ b, c, e ];
e.neighbors = [ d ];

module.exports = searchengine;
