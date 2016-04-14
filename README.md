# subsearch
subsearch is a command line tool designed to discover subdomain names. It is aimed at penetration testers and bug
bounty hunters and has been built with a focus on speed, stealth and reporting.

The current release is version 0.2.0 and was published on 14/4/2016.

## Features

- Scan a single hostname or a list of hostnames
- Takes as arguments a comma separated list of DNS resolvers, and/or a file containing newline delimited list of resolvers
- Recursive scanning: If a CNAME, MX, NS or SRV record is discovered, the any subdomains will be added to a priority list
of subdomains to scan for
- Support for additional scanners:
  - Attempt a Zone Transfer on the hostname's authoritative name servers
  - Retrieve seen subdomains from Virus Total
  - Retrieve seen subdomains from DNS Dumpster
- Different levels of verbosity
- Multiple real-time reporting capabilities
- Supports the use of massive wordlists

## Requirements

subsearch is built in scala using the Java 8 SDK.

## How to use

A compiled copy of the latest version can be downloaded from the [releases page](https://github.com/gavia/subsearch/releases).
Alternatively you can clone the latest commit and compile it by executing `sbt assembly` in the root folder. The compiled
jar will be located in `target/scala-2.11/`.

To show the below help text, execute `java -jar <subsearch jar file> --help`.

```
subsearch 0.2.0
Usage: subsearch [options]

Options:
  --help
        Prints this usage text.

Mandatory:
  -h HOSTNAME | --hostname HOSTNAME
        The hostname(s) to scan. Enter more than one by separating with a comma.
and/or
  -H HOSTLIST | --hostlist HOSTLIST
        A file containing a newline delimited list of hostnames to scan.
  -w WORDLIST | --wordlist WORDLIST
        A newline delimited list of subdomain names.
  -r RESOLVERS | --resolvers RESOLVERS
        The name server(s) to scan with. Enter more than one by separating with a comma.
and/or
  -R RESOLVERSLIST | --resolverslist RESOLVERSLIST
        A file containing a newline delimited list of name servers to scan with.

General Settings:
  -a | --auth-resolvers
        Include the hostname's authoritative name servers in the list of resolvers. Defaults to false.
  -c | --concurrent-resolver-requests
        Allow for more than one request to each resolver at the same time. If true, it can result in being blacklisted or rate limited by some resolvers. Defaults to false.
  -t THREADCOUNT | --threads THREADCOUNT
        The number of concurrent threads whilst scanning. Defaults to 10.
  -v | --verbose
        Show more extended command line output such as the addresses that A, AAAA and CNAME records point to. Defaults to false.
  --comprehensive
        Runs all additional scanners.

Additional Scanners:
  --dns-dumpster
        Attempts to lookup possible records from dnsdumpster.com
  --virus-total
        Attempts to lookup possible records from virustotal.com
  -z | --zone-transfer
        Attempts a zone transfer against the host's authoritative name servers.

Reporting:
  --report-csv OUTPUTFILE
        Outputs a CSV report of discovered subdomains including timestamp, subdomain, record type and record data.
  --report-stdout OUTPUTFILE
        Outputs standard out to a file.

```

Subdomain and resolvers lists are not bundled with this tool as excellent resources already exist in other locations.
For those that do not already possess these resources, [fuzzdb](https://github.com/fuzzdb-project/fuzzdb) and
[subbrute](https://github.com/TheRook/subbrute) are good places to start.

## Issues

If you have any problems or questions please open an issue and I'll try to help. If you're going to submit a bug, please
provide steps to reproduce the issue and a copy of the program output.

## New features

Contributions and suggestions are welcome! I'm going to continue to update this tool with new features, but if you have
an idea for some great functionality then open an issue, or alternatively give it a go yourself and create a pull request.
For a list of things that are already on my roadmap, checkout the `TODO` file.

## Changelog

- 0.2.0 - 14/4/2016
  - Added Virus Total and DNS Dumpster as additional scanners
  - Zone Transfer is now off by default
  - Added an option to report standard out to file
  - Numerous bug fixes
  - Began writing tests
  - Removed check to see if domain is "valid"
- 0.1.1 - 14/3/2016
  - subsearch can now handle massive wordlists, wordlists aren't loaded into memory in one go
  - resolver timeouts increased from 5, 10 and 15 seconds to 10, 20 and 30 seconds
  - other minor bug fixes
- 0.1.0 - 14/3/2016 - Initial release

## License

This tool is released under the GNU General Public License, version 2. A copy of this license can be found in the `LICENSE`
file.

The design for the user interface has been inspired by the fantastic tool [dirsearch](https://github.com/maurosoria/dirsearch)
by Mauro Soria. Anything that could be considered a direct copy of his tool is Copyright (C) Mauro Soria.

All other work is Copyright (C) Gil Azaria.
