#!/bin/bash

source /etc/os-release

DOCKER_CRI_VERSION="v0.3.10/"
if [[ $ID == "debian" ]]; then
	DOCKER_CRI_FILE="cri-dockerd_0.3.10.3-0.debian-bullseye_amd64.deb"
elif [[ $ID == "ubuntu" ]]; then
	DOCKER_CRI_FILE="cri-dockerd_0.3.10.3-0.ubuntu-jammy_amd64.deb"
fi

message() {
  local TEXT=$1
	local COLOR='\033[38;5;220m'
    local RESET='\033[0m'

    echo -e "${COLOR}[nextCluster] ${TEXT}${RESET}"
}


# Update service
message 'Updating system...'

DEBIAN_FRONTEND=noninteractive apt-get install sudo
sudo DEBIAN_FRONTEND=noninteractive apt-get update
sudo DEBIAN_FRONTEND=noninteractive apt-get upgrade -y
sudo DEBIAN_FRONTEND=noninteractive apt-get install wget -y

if command -v docker &> /dev/null; then
	message 'Docker found, skipping installation!'
else
  # Install docker
	message 'Docker not found, installing...'

	sudo DEBIAN_FRONTEND=noninteractive apt-get update
	sudo DEBIAN_FRONTEND=noninteractive apt-get install ca-certificates curl
	sudo install -m 0755 -d /etc/apt/keyrings
 	if [[ $ID == "debian" ]]; then
		sudo curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
  	elif [[ $ID == "ubuntu" ]]; then
		sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
   	fi
	sudo chmod a+r /etc/apt/keyrings/docker.asc

	if [[ $ID == "debian" ]]; then
		echo \
		  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian \
		  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
		  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  	 elif [[ $ID == "ubuntu" ]]; then
   		 echo \
		  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
		  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
		  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    	fi
	sudo DEBIAN_FRONTEND=noninteractive apt-get update
	sudo DEBIAN_FRONTEND=noninteractive apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y
fi

# Download docker-cri as container runtime
CRI_SOCKET="/run/cri-dockerd.sock"

if [ -e "$CRI_SOCKET" ]; then
  message 'Docker-CRI found, skipping installation!'
else
  message "Docker-CRI  not found, installing... ($DOCKER_CRI_VERSION$DOCKER_CRI_FILE)"

  sudo wget --quiet https://github.com/Mirantis/cri-dockerd/releases/download/$DOCKER_CRI_VERSION$DOCKER_CRI_FILE
  sudo dpkg -i $DOCKER_CRI_FILE
  sudo rm $DOCKER_CRI_FILE
fi

swapoff -a
DEBIAN_FRONTEND=noninteractive apt-get install gnupg gnupg1 gnupg2 -y

# Add Kubernetes-Repository
sudo DEBIAN_FRONTEND=noninteractive apt-get update && sudo DEBIAN_FRONTEND=noninteractive DEBIAN_FRONTEND=noninteractive apt-get install -y apt-transport-https -y
wget --quiet https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key --keyring /usr/share/keyrings/cloud.google.gpg add -
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee /etc/apt/sources.list.d/kubernetes.list > /dev/null

KUBELET_CONF="/etc/kubernetes/kubelet.conf"
if [ -e "$KUBELET_CONF" ]; then
  message 'Kubernetes found, skipping installation!'
else
  message 'Kubernetes not found, installing...'

  # Install Kubernets tools
  sudo DEBIAN_FRONTEND=noninteractive apt-get update
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y kubectl kubelet kubeadm

  # Initialize Kubernetes cluster
  sudo kubeadm init --pod-network-cidr=192.168.0.0/16 --cri-socket=unix:///var/run/cri-dockerd.sock

  # Copy kubeconfig for kubectl
  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  # shellcheck disable=SC2046
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

  # Add Calico as pod-network plugin
  kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml

  JOIN_COMMAND=$(kubeadm token create --print-join-command)

  # Remove taint on all nods to start pods on these
  kubectl taint nodes --all node-role.kubernetes.io/control-plane-
  message "Worker-Join-Command: $JOIN_COMMAND"
fi

message "Initializing nextCluster (namespace: nextCluster)..."
kubectl apply -f https://raw.githubusercontent.com/nextCluster/nextCluster/master/scripts/init-cluster.yml
message "nextCluster installation finished!"
