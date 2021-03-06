#!/bin/bash
#
# Generates <compound-name>.svg files for each compound.
#

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
OUTDIR="${DIR}/../src/main/resources/static/img/smiles"

if [ ! -f "${DIR}/env.sh" ]; then
    echo "./env.sh not found.  Please create it to continue"
    exit 1
fi
. "${DIR}/env.sh"

if ! command -v obabel &>/dev/null ; then
  echo "obabel is not on your PATH.  Exiting without generating SMILES svg files"
  exit 0
fi

rm -fr "${OUTDIR}"
mkdir "${OUTDIR}"

TEMP_CSV=$(mktemp)
PGPASSWORD=${PASSWORD} psql -h "${HOST}" -U "${USER}" -c "\copy (select compound_nm, smiles from ${SCHEMA}.compound where smiles is not null order by compound_nm) to stdout with csv" ${DB} > "${TEMP_CSV}"

echo "Generating SMILES strings svg files, please be patient..."
SMILES_LOG=${OUTDIR}/smile-generation.log
while read LINE ; do
    # Be sure to escape chars that are problematic in file names...
    COMPOUND="$( cut -d ',' -f 1 <<< "${LINE}" | tr / _ )"
    SMILES="$( cut -d ',' -f 2- <<< "${LINE}" )"
    echo "Compound: ${COMPOUND}, smiles: ${SMILES}..." >> ${SMILES_LOG}
    obabel -:"${SMILES}" -O "${OUTDIR}/${COMPOUND}.svg" -xb none >> ${SMILES_LOG} 2>&1
done < "${TEMP_CSV}"
echo "Done.  See ${SMILES_LOG} for a log of the SMILES svg generation"
echo "Compounds with errors: $(grep -ci error "${SMILES_LOG}")"
echo "Compounds with warnings: $(grep -ci warning "${SMILES_LOG}")"

rm "${TEMP_CSV}"
