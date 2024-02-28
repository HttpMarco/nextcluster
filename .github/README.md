<div align="center">
  <img src="../.img/logo_middle.png" alt="nextCluster">
</div>

# Installation

### Approved operating system: Debian 12/11/10
For <b><u> approved operating system</u></b> users, you can install the package from the repository by running the following command:
```bash
wget https://raw.githubusercontent.com/nextCluster/nextCluster/master/scripts/install-cluster.sh && chmod +x install-cluster.sh && ./install-cluster.sh && rm install-cluster.sh
```

### Other
You have to install these [dependencies](https://wiki.nextcluster.net/docs/installation/dependencies) on your os.
<br/>When you have installed the dependencies, you can install the package from the repository by running the following command:
```bash
kubectl apply -f https://raw.githubusercontent.com/nextCluster/nextCluster/master/scripts/init-cluster.yml
```

# Planned in the future
- [ ] (install-script) Add support for other operating systems
- [ ] Dashboard
- [ ] Proxy-Module

# Contributing
You can contribute to the project by forking the repository and creating a pull request. You can also contribute by creating an issue.
Pull Requests are welcome.<br/>
For major changes, please open an issue first to discuss what you would like to change.

****
<p align="center">
    Readme work in progress...
</p>
