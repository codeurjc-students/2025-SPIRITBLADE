#!/bin/bash
# ==============================================================================
# Script de inicializacion para MySQL en Oracle Linux ARM64
# ==============================================================================

set -e

# 1. Deshabilitar Firewall y SELinux inmediatamente
systemctl stop firewalld || true
systemctl disable firewalld || true
setenforce 0 || true
if [ -f /etc/selinux/config ]; then
    sed -i 's/^SELINUX=.*/SELINUX=disabled/g' /etc/selinux/config
fi

# Flush iptables
iptables -F || true
iptables -X || true
iptables -t nat -F || true
iptables -t nat -X || true
iptables -P INPUT ACCEPT || true
iptables -P FORWARD ACCEPT || true
iptables -P OUTPUT ACCEPT || true

# 2. Instalar MySQL
if ! rpm -q mysql-server; then
    dnf install -y mysql-server
fi

# 3. Configurar MySQL
mkdir -p /etc/my.cnf.d

cat > /etc/my.cnf.d/z-remote.cnf <<-EOF
[mysqld]
bind-address = 0.0.0.0
mysqlx-bind-address = 0.0.0.0
port = 3306
skip-networking = 0
EOF

# 4. Iniciar servicio
systemctl enable mysqld
systemctl start mysqld

# 5. Esperar disponibilidad
echo 'Esperando a MySQL...'
RETRIES=0
until mysqladmin ping -h localhost --silent; do
    echo 'Waiting for MySQL...'
    sleep 2
    RETRIES=$((RETRIES+1))
    if [ $RETRIES -gt 30 ]; then exit 1; fi
done

# 6. Crear usuarios
mysql -u root <<-MYSQL_SCRIPT
    CREATE DATABASE IF NOT EXISTS spiritblade;
    CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'SpiritBlade2024!';
    GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
    FLUSH PRIVILEGES;
MYSQL_SCRIPT

echo 'DONE'
