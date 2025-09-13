{
  description = "Nix flake for the Obliviate project.";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs";
    flake-utils.url = "github:numtide/flake-utils";
    fenix = {
      url = "github:nix-community/fenix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
    fenix,
    ...
  }:
    flake-utils.lib.eachDefaultSystem (system: let
      # Lock the Rust toolchain to the one specified in the repo.
      rustToolchain = fenix.packages.${system}.fromToolchainFile {
        dir = ./.;
        sha256 = "sha256-bz3WHxOpv8RFH754gIn2WUpDCDzxA1JHqjsb+C5uJiQ=";
      };

      # Bring in httpgd 2.0.3 since 2.0.4 is marked as broken.
      overlay = final: prev: {
        rPackages =
          prev.rPackages
          // {
            httpgd = prev.rPackages.buildRPackage {
              name = "httpgd";

              src = prev.fetchurl {
                url = "https://cran.r-project.org/src/contrib/Archive/httpgd/httpgd_2.0.3.tar.gz";
                sha256 = "sha256-vVp4T3zVR5v6KRMUiCy5/aLen0t+aX3ODl/99UBp6Qo=";
              };

              propagatedBuildInputs = with prev.rPackages; [
                unigd
                cpp11
                AsioHeaders
              ];
            };
          };
      };

      pkgs = import nixpkgs {
        inherit system;
        config.allowUnfree = true;
        overlays = [overlay];
      };
    in {
      devShell = with pkgs;
        mkShell {
          buildInputs = [
            claude-code
          ];
        };
    });
}
