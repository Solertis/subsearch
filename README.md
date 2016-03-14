# subsearch
subsearch is a command line tool designed to brute force subdomain names. It is aimed at penetration testers and bug
bounty hunters and has been built with a focus on speed, stealth and reporting.

The current release is version 0.1.0 and was published on 14/3/2016.

## Features

- Scan a single hostname or a list of hostnames
- Takes as arguments a comma separated list of DNS resolvers, and/or a file containing newline delimited list of resolvers
- Check if the hostname's authoritative name servers are vulnerable to a zone transfer (can be skipped)
- Recursive scanning: If a CNAME, MX, NS or SRV record is discovered, the any subdomains will be added to a priority list
of subdomains to scan for
- Extra level of verbosity
- Reporting capability
- Real-time feedback

## How to use

A compiled copy of the latest version can be downloaded from the [releases page](https://github.com/gavia/subsearch/releases).
Alternatively you can clone the latest commit and compile it by executing `sbt assembly` in the root folder. The compiled
jar will be located in `target/scala-2.11/`.

To show the below help text, execute `java -jar <subsearch jar file> --help`.

```
subsearch 0.1.0
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
  -z | --no-zone-transfer
        Avoids attempting a zone transfer against the host's authoritative name servers.

Reporting:
  --csv-report OUTPUTFILE
        Outputs a CSV report of discovered subdomains including timestamp, subdomain, record type and record data.

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

- 0.1.0 - 14/3/2016 - Initial release

## License

This tool is released under the GNU General Public License, version 2. A copy of this license can be found in the `LICENSE`
file.

The design for the user interface has been inspired by the fantastic tool [dirsearch](https://github.com/maurosoria/dirsearch)
by Mauro Soria. Anything that could be considered a direct copy of his tool is Copyright (C) Mauro Soria.