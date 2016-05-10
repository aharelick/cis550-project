var Node         = require('../models/Node.js');
var InvertedNode = require('../models/InvertedNode.js');

var NUM_RESULTS = 10;
var MAX_DEPTH = 10;


// search for minimal paths from start to end
var search = function(start, end, callback) {
    if (end.neighbors.indexOf(start._id) != -1) {
        callback([[ start, end ]]);
        return;
    }
    var seen = [];
    var done = [];
    done[start._id] = false;

    start._depth   = 0;
    start._parents = [];

    end._depth     = Number.MAX_VALUE;
    end._parents   = [];
    seen[end._id]  = end;

    dfs(start, null, seen, done, function() {
        for (var key in done) {
            if (done[key] === false) return;
        }
        var paths = generate(end);
        var out   = [];
        for (var i = 0; i < paths.length; i++) {
            if (paths[i].length > 1) out.push(paths[i]);
        }
        callback(out);
    });
}

// limited depth-first search used by search
var dfs = function(start, last, seen, done, callback) {
    seen[start._id] = start;

    var depth;
    if (last != null) depth = last._depth + 1;
    else              depth = 0;


    if (depth === MAX_DEPTH) {
        done[start._id] = true;
        callback();
        return;
    }
    if (depth < start._depth) {
        start._depth = depth;
        if (last !== null) start._parents = [ last ];
        else               start._parents = [ ];
    } else if (depth === start._depth && last !== null) {
        start._parents.push(last);
    } else if (depth > start._depth) {
        done[start._id] = true;
        callback();
        return;
    }

    for (var i = 0; i < start.neighbors.length; i++) {

        done[start.neighbors[i]] = false;

        if (start.neighbors[i] in seen) {
            dfs(seen[start.neighbors[i]], start, seen, done, callback);
        } else {
            Node.findOne({ _id: start.neighbors[i] }, function(err, node) {
                if (node._id in seen) {
                    dfs(seen[node._id], start, seen,  done, callback);
                    return;
                }
                node._depth   = Number.MAX_VALUE;
                node._parents = [ ];
                dfs(node, start, seen, done, callback);
            });
        }
    }

    done[start._id] = true;
    callback();
}


// generates path from dfs
var generate = function(end) {
    var paths = [];
    if (end._parents.length === 0) {
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


    return paths;
}

// produces an iterator to find all possible path endpoints
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

// get k shortest paths between the pairs
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
        pair = pairs.next();
    }
}

// finds path from nodes to their roots
var toroot = function(nodes, callback) {
    var map = {
        check: function() {
            for (var i = 0; i < nodes.length; i++) {
                if (this[nodes[i]._id] == false) return false;
            }
            return true;
        }
    };
    for (var i = 0; i < nodes.length; i++) {
        map[nodes[i]._id] = false;
    }

    var up = function(end, path, base) {
        path.push(end);
        if (end.parent == null) {
            map[base] = path;
            if (map.check()) {
                var results = [];
                for (var i = 0; i < nodes.length; i++) {
                    results.push(map[nodes[i]._id]);
                }
                callback(results)
            }
        } else {
            Node.findOne({ _id: end.parent }, function(err, parent) {
                up(parent, path, base);
            });
        }
    }

    for (var i = 0; i < nodes.length; i++) {
        up(nodes[i], [], nodes[i]._id);
    }
}

// takes query and redirectrs to appropriate method
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
        return function(err, inodes) {
            if (err) console.log(err);
            else {
                var ids = [];
                for (var i = 0; i < inodes.length; i++) {
                    ids.push(inodes[i].nodeId);
                }
                Node.find({ _id: { $in: ids }}, function(err, nodes) {
                    finished[token] = true;
                    if (nodes.length > 0) results.push(nodes);
                    if (finished.check()) {
                        if (results.length == 0) callback([]);
                        if (results.length == 1) toroot(results[0], callback);
                        else topk(cartesian(results), NUM_RESULTS, callback);
                    }
                });
            }
        }
    }
    for (var i = 0; i < tokens.length; i++) {
        InvertedNode.find({ term: tokens[i] },
                          assemble(tokens[i], results, finished, callback));
    }
}

module.exports = searchengine;
