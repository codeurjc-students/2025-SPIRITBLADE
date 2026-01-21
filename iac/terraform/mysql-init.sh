#!/bin/bash
# ==============================================================================
# Script de inicialización para MySQL en Oracle Linux ARM64
# ==============================================================================

set -e

echo "==> Actualizando sistema..."
dnf update -y

echo "==> Instalando MySQL 8.0..."
dnf install -y mysql-server

echo "==> Iniciando MySQL..."
systemctl enable mysqld
systemctl start mysqld

echo "==> Esperando a que MySQL esté listo..."
until mysqladmin ping -h localhost --silent; do
    echo "Esperando MySQL..."
    sleep 2
done

echo "==> Configurando MySQL..."

# Configurar contraseña root
mysql -u root <<-EOSQL
    ALTER USER 'root'@'localhost' IDENTIFIED BY '${mysql_root_password}';
    CREATE DATABASE IF NOT EXISTS ${mysql_database};
    CREATE USER IF NOT EXISTS 'spiritblade'@'%' IDENTIFIED BY '${mysql_root_password}';
    GRANT ALL PRIVILEGES ON ${mysql_database}.* TO 'spiritblade'@'%';
    FLUSH PRIVILEGES;
EOSQL

# Configurar MySQL para aceptar conexiones remotas
cat > /etc/my.cnf.d/remote.cnf <<-EOF
[mysqld]
bind-address = 0.0.0.0
max_connections = 200
EOF

echo "==> Reiniciando MySQL con nueva configuración..."
systemctl restart mysqld

echo "==> Configurando firewall..."
firewall-cmd --permanent --add-service=mysql
firewall-cmd --reload

echo "==> MySQL instalado y configurado correctamente"
echo "==> Base de datos: ${mysql_database}"
echo "==> Usuario root configurado"

# Crear un archivo de estado
echo "MySQL instalado exitosamente el $(date)" > /root/mysql-setup-complete.txt
