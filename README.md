# feel-dns
dns server check zk to answer

# info
1. config zk server
2. start flash(a reverse proxy in java)
3. start feel-dns

# zk data
1. persistent znodes
/upstreams             
/upstreams/config -> check config change
/upstreams/names  -> proxy support name
/lock             -> change config lock
2. ephemeral znodes
your proxy name
/upstreams/names/flash.jt.com
one proxy
/upstreams/names/flash.qunar.com/192.168.1.2
another proxy
/upstreams/names/flash.qunar.com/192.168.1.3
