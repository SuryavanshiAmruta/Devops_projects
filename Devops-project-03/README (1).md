# Kubernetes Application Deployment with Prometheus & Grafana Monitoring on AWS EC2

A hands-on DevOps project that demonstrates how to containerize a simple Nginx web application, deploy it to Kubernetes on an AWS EC2 instance, expose it through a NodePort Service, and monitor the Kubernetes environment with Prometheus and Grafana.

## Project Overview

This project deploys a static web application using Docker and Kubernetes on an Ubuntu-based AWS EC2 instance. Kubernetes manages the application pods, while Prometheus and Grafana provide metrics collection and visualization.

The project documentation describes the setup of a single-node Kubernetes cluster using `kubeadm`, installation of the Calico network plugin, and deployment of the Prometheus community monitoring stack through Helm.

## Objectives

- Prepare an Ubuntu EC2 instance for a Kubernetes environment.
- Build a Docker image for an Nginx-based web application.
- Deploy the application with a Kubernetes Deployment.
- Expose the application using a Kubernetes NodePort Service.
- Scale the application by changing the Deployment replica count.
- Install Prometheus and Grafana using Helm for monitoring.
- Practice Kubernetes verification and troubleshooting.
- Manage project files using Git and GitHub.

## Architecture

```text
Developer / VS Code
        |
        v
   GitHub Repository
        |
        v
 AWS EC2 - Ubuntu 24.04 LTS
        |
        +-----------------------------+
        |                             |
        v                             v
 Docker image                    Kubernetes
 build: myapp:v1                 single-node cluster
                                      |
                                      v
                               Deployment: myapp
                                  replicas: 2
                                      |
                                      v
                                  Pods (Nginx)
                                      |
                                      v
                           Service: NodePort 30080
                                      |
                                      v
                              Web application

 Monitoring stack (installed separately using Helm):
 Kubernetes / node metrics -> Prometheus -> Grafana dashboards
```

## Technologies Used

| Technology | Purpose |
|---|---|
| AWS EC2 | Hosts the Linux server and Kubernetes environment |
| Ubuntu 24.04 LTS | Operating system |
| Docker | Builds the application container image |
| Kubernetes | Deploys and manages application pods |
| kubeadm | Initializes the Kubernetes cluster |
| kubectl | Manages Kubernetes resources |
| Calico | Kubernetes pod networking |
| Helm | Installs and manages Kubernetes packages |
| Prometheus | Collects and stores metrics |
| Grafana | Visualizes monitoring metrics |
| Nginx | Serves the static web application |
| Git and GitHub | Version control and source-code hosting |
| VS Code | Code editor |

## Repository Structure

```text
Devops-project-03/
├── Dockerfile
├── index.html
├── deployment.yaml
├── service.yaml
└── README.md
```

## Application Details

The application is a simple static HTML page served by Nginx.

### Dockerfile

```dockerfile
FROM nginx
COPY index.html /usr/share/nginx/html
```

### Kubernetes Deployment

The `deployment.yaml` file creates a Deployment named `myapp` with two replicas. Each pod runs the container image `myapp:v1` and exposes container port `80`.

### Kubernetes Service

The `service.yaml` file creates a NodePort Service named `myapp-service`. It forwards traffic from service port `80` to container port `80` and uses NodePort `30080`.

## Prerequisites

- An AWS account.
- An Ubuntu 24.04 LTS EC2 instance with sufficient CPU, memory, and disk for Kubernetes and the monitoring stack.
- SSH access to the EC2 instance.
- Docker, kubeadm, kubelet, kubectl, containerd, Helm, and a Kubernetes CNI plugin installed as described below.
- Inbound access configured carefully in the EC2 Security Group.

> **Security:** Do not expose Kubernetes API, Grafana, Prometheus, or SSH ports publicly to everyone. Restrict administrative and monitoring access to your own IP address or use an SSH tunnel. The application NodePort is `30080`; allow it only from the clients that need to reach the application.

## Implementation

The following steps reflect the setup procedure documented for this project. Run the commands on the EC2 instance unless a step says otherwise.

### 1. Launch and update the EC2 instance

Launch an Ubuntu 24.04 LTS EC2 instance, connect through SSH, and update packages:

```bash
sudo apt update
sudo apt upgrade -y
```

### 2. Install Docker

```bash
sudo apt install docker.io -y
sudo systemctl enable docker
sudo systemctl start docker
docker --version
```

### 3. Install Kubernetes tools

Install the required packages:

```bash
sudo apt install -y apt-transport-https curl gpg
sudo mkdir -p /etc/apt/keyrings
```

Add the Kubernetes package signing key and repository (the project notes use Kubernetes v1.32):

```bash
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.32/deb/Release.key \
  | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.32/deb/ /' \
  | sudo tee /etc/apt/sources.list.d/kubernetes.list
```

Install and hold the Kubernetes packages:

```bash
sudo apt update
sudo apt install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
kubectl version --client
```

### 4. Install and configure containerd

```bash
sudo apt install -y containerd
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml
sudo systemctl restart containerd
sudo systemctl enable containerd
```

### 5. Initialize the Kubernetes control plane

```bash
sudo kubeadm init
```

After initialization completes, configure `kubectl` for the current user:

```bash
mkdir -p "$HOME/.kube"
sudo cp -i /etc/kubernetes/admin.conf "$HOME/.kube/config"
sudo chown "$(id -u):$(id -g)" "$HOME/.kube/config"
```

### 6. Install the Calico network plugin

```bash
kubectl apply -f https://raw.githubusercontent.com/projectcalico/calico/v3.29.0/manifests/calico.yaml
kubectl get pods -A
```

Wait for the cluster networking pods to become ready.

### 7. Allow workloads on the single-node control plane

For the single-node cluster described in the project documentation:

```bash
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
```

### 8. Install Helm

```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
helm version
```

### 9. Install Prometheus and Grafana

Create a monitoring namespace:

```bash
kubectl create namespace monitoring
```

Add and update the Prometheus community Helm repository:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```

Install the Kubernetes monitoring stack:

```bash
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring
```

Check the monitoring pods:

```bash
kubectl get pods -n monitoring
kubectl get svc -n monitoring
```

The chart installs components such as Prometheus, Alertmanager, Grafana, Node Exporter, and kube-state-metrics.

Retrieve the Grafana admin password:

```bash
kubectl get secret --namespace monitoring prometheus-grafana \
  -o jsonpath="{.data.admin-password}" | base64 --decode
echo
```

For local access from the EC2 host, port-forward Grafana:

```bash
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
```

To access it securely from your own computer, create an SSH tunnel to the EC2 instance and open `http://localhost:3000` in your browser. Avoid exposing Grafana directly to the public internet.

### 10. Build the application Docker image

From the project directory on the EC2 instance, build the image:

```bash
docker build -t myapp:v1 .
docker images
```

**Important:** The Kubernetes manifest references the local image name `myapp:v1`. Make sure the image is available to the container runtime used by Kubernetes on the node. If Docker and kubelet/containerd use separate image stores, build or import the image into the runtime Kubernetes uses, or publish it to a container registry and update `deployment.yaml` with the registry image name.

### 11. Deploy the application

Apply the Deployment:

```bash
kubectl apply -f deployment.yaml
```

Apply the Service:

```bash
kubectl apply -f service.yaml
```

### 12. Verify the deployment

```bash
kubectl get nodes
kubectl get deployments
kubectl get pods -o wide
kubectl get services
```

Wait until the application pods show `Running` and the Deployment reports the expected available replicas.

### 13. Access the application

Open the following address in a browser, replacing `<EC2-PUBLIC-IP>` with the instance's public IPv4 address:

```text
http://<EC2-PUBLIC-IP>:30080
```

The EC2 Security Group must allow inbound TCP traffic on port `30080` from your intended client IP range.

## Monitoring with Prometheus and Grafana

The project documentation uses the `kube-prometheus-stack` Helm chart. After installation:

- Check monitoring workloads with `kubectl get pods -n monitoring`.
- Check available monitoring services with `kubectl get svc -n monitoring`.
- Access Grafana through the port-forward or SSH-tunnel method above.
- Sign in with username `admin` and the password retrieved from the Kubernetes Secret.
- Explore the Kubernetes and Node Exporter dashboards available in Grafana.
- Use Prometheus as the metrics source for dashboard visualizations.

The project notes describe monitoring node memory, container memory, disk space, and network traffic.

## Scaling and Deployment Management

The project documentation identifies scaling, rolling updates, and rollback as learning objectives. The checked-in application Deployment starts with two replicas. You can practice these Kubernetes operations:

```bash
# Scale the application to three replicas
kubectl scale deployment myapp --replicas=3

# Check rollout status
kubectl rollout status deployment/myapp

# View rollout history
kubectl rollout history deployment/myapp

# Roll back to the previous revision, if one exists
kubectl rollout undo deployment/myapp
```

## Troubleshooting

| Issue | Checks / suggested action |
|---|---|
| Node shows `NotReady` | Check `kubectl get nodes`, `sudo systemctl status kubelet`, and `kubectl get pods -A`; inspect kubelet logs before restarting services. |
| Prometheus target is down | Check monitoring pods and services with `kubectl get pods,svc -n monitoring`; inspect the relevant pod logs. |
| Grafana shows no data | Verify that the Prometheus data source is configured and healthy; check Prometheus targets. |
| Pod shows `ImagePullBackOff` | Check the image name, tag, registry credentials, and whether the image is accessible to the Kubernetes container runtime. |

Useful diagnostic commands:

```bash
kubectl describe pod <POD-NAME>
kubectl logs <POD-NAME>
kubectl get events --sort-by=.metadata.creationTimestamp
kubectl get pods -A
kubectl get svc -A
```

## What I Learned

- Preparing and administering an AWS EC2 Linux environment.
- Building a container image with Docker.
- Creating Kubernetes Deployments and Services using YAML.
- Running multiple application replicas.
- Exposing an application through a NodePort Service.
- Installing Kubernetes monitoring components with Helm.
- Understanding Prometheus metrics collection and Grafana dashboards.
- Verifying deployments and troubleshooting common Kubernetes issues.
- Managing project source files with Git and GitHub.

## Current Repository Scope and Notes

The repository files reviewed for this README contain the Nginx `Dockerfile`, `index.html`, Kubernetes `deployment.yaml`, and `service.yaml`. The Prometheus and Grafana setup is described in the accompanying project documentation and is installed through Helm commands; separate Prometheus/Grafana YAML manifests are not present in this project folder.

The project documentation mentions both Minikube and a `kubeadm`-initialized cluster in different sections. The implementation steps in this README follow the detailed `kubeadm` setup procedure. Confirm the cluster method actually used in your environment before presenting it as completed work.

## Author

**Amruta Suryavanshi**

GitHub: [SuryavanshiAmruta](https://github.com/SuryavanshiAmruta)

Repository: [Devops_projects](https://github.com/SuryavanshiAmruta/Devops_projects)

---

If you found this project useful, feel free to explore the repository and its other DevOps projects.
