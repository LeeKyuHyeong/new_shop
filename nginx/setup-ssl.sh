#!/bin/bash
# shop.kiryong.com SSL Setup Script
# Run on production server as root

set -e

DOMAIN="shop.kiryong.com"
NGINX_CONF_DIR="/etc/nginx/conf.d"
CONF_FILE="$NGINX_CONF_DIR/$DOMAIN.conf"

echo "=== Setting up SSL for $DOMAIN ==="

# 1. Copy nginx config
echo "[1/4] Copying nginx configuration..."
cp shop.kiryong.com.conf $CONF_FILE

# 2. Test nginx config (before SSL - comment out SSL lines temporarily)
echo "[2/4] Testing nginx configuration..."
sed -i.bak 's/^[^#]*ssl_/# &/' $CONF_FILE
sed -i 's/^[^#]*include.*letsencrypt/# &/' $CONF_FILE
sed -i 's/listen 443/# listen 443/' $CONF_FILE

nginx -t
systemctl reload nginx

# 3. Get SSL certificate
echo "[3/4] Obtaining SSL certificate..."
certbot --nginx -d $DOMAIN --non-interactive --agree-tos --email admin@kiryong.com

# 4. Restore original config and reload
echo "[4/4] Finalizing..."
mv $CONF_FILE.bak $CONF_FILE 2>/dev/null || true
nginx -t && systemctl reload nginx

echo ""
echo "=== Setup Complete ==="
echo "Site: https://$DOMAIN"
echo ""
echo "Useful commands:"
echo "  - Check status: systemctl status nginx"
echo "  - View logs: tail -f /var/log/nginx/$DOMAIN.*.log"
echo "  - Renew SSL: certbot renew --dry-run"
